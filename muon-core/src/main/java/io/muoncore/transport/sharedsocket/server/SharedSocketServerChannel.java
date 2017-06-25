package io.muoncore.transport.sharedsocket.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.transport.sharedsocket.client.SharedSocketRouter;
import io.muoncore.transport.sharedsocket.client.messages.SharedChannelInboundMessage;
import io.muoncore.transport.sharedsocket.client.messages.SharedChannelOutboundMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The concrete muon socket, acts as the multipler/ server router.
 */
public class SharedSocketServerChannel implements ChannelConnection<MuonInboundMessage, MuonOutboundMessage> {

    private Logger logger = LoggerFactory.getLogger(SharedSocketServerChannel.class);

    private ChannelFunction<MuonOutboundMessage> outboundFunc;

    private ServerStacks stacks;
    private Codecs codecs;
    private Map<String, ChannelConnection<MuonInboundMessage, MuonOutboundMessage>> localConnections = new HashMap<>();

    public SharedSocketServerChannel(ServerStacks stacks, Codecs codecs) {
        this.codecs = codecs;
        this.stacks = stacks;
    }

    @Override
    public void receive(ChannelFunction<MuonOutboundMessage> function) {
        this.outboundFunc = function;
    }

    @Override
    public void send(MuonInboundMessage message) {
        if (message == null) {
            return;
        }

        if (message.getChannelOperation() == MuonMessage.ChannelOperation.closed) {
            logger.debug("Received a channel op closed message " + message);
            return;
        }

        SharedChannelInboundMessage msg = codecs.decode(message.getPayload(), message.getContentType(), SharedChannelInboundMessage.class);

        ChannelConnection<MuonInboundMessage, MuonOutboundMessage> connection = getConnectionToProtocol(msg);
        connection.send(msg.getMessage());
    }

    private ChannelConnection<MuonInboundMessage, MuonOutboundMessage> getConnectionToProtocol(SharedChannelInboundMessage msg) {
        ChannelConnection<MuonInboundMessage, MuonOutboundMessage> protocolConnection = localConnections.get(msg.getChannelId());

        if (protocolConnection == null) {
            protocolConnection = stacks.openServerChannel(msg.getMessage().getProtocol());
            protocolConnection.receive(arg -> {

                if (arg == null || arg.getChannelOperation() == MuonMessage.ChannelOperation.closed) {
                    return;
                }

                SharedChannelOutboundMessage sharedMsg = new SharedChannelOutboundMessage(msg.getChannelId(), arg);
                Codecs.EncodingResult result = codecs.encode(sharedMsg, new String[] { "application/json"});

                MuonOutboundMessage outMsg = MuonMessageBuilder.fromService(arg.getSourceServiceName())
                        .contentType(result.getContentType())
                        .payload(result.getPayload())
                        .protocol(SharedSocketRouter.PROTOCOL)
                        .step("message")
                        .toService(arg.getTargetServiceName()).build();
                outboundFunc.apply(outMsg);
            });
            localConnections.put(msg.getChannelId(), protocolConnection);
        }

        return protocolConnection;
    }

    @Override
    public void shutdown() {
      localConnections.values().forEach(ChannelConnection::shutdown);
        //TODO, handle this, closing all sockets and protocol connections..
    }
}
