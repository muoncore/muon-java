package io.muoncore.transport.sharedsocket.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.transport.sharedsocket.client.messages.SharedChannelInboundMessage;
import io.muoncore.transport.sharedsocket.client.messages.SharedChannelOutboundMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The concrete muon socket, acts as the multipler/ server router.
 */
public class SharedSocketServerChannel implements ChannelConnection<MuonInboundMessage, MuonOutboundMessage> {

    private UUID id = UUID.randomUUID();
    private Logger logger = LoggerFactory.getLogger(SharedSocketServerChannel.class);

    private ChannelFunction<MuonOutboundMessage> outboundFunc;

    private ServerStacks stacks;
    private Codecs codecs;
    private Map<String, ChannelConnection<MuonInboundMessage, MuonOutboundMessage>> localConnections = new HashMap<>();
    private boolean shutdown = false;

    public SharedSocketServerChannel(ServerStacks stacks, Codecs codecs) {
        this.codecs = codecs;
        this.stacks = stacks;
        logger.debug("Created new shared channel {}", id);
    }

    @Override
    public void receive(ChannelFunction<MuonOutboundMessage> function) {
        this.outboundFunc = function;
    }

    @Override
    public void send(MuonInboundMessage message) {
        if (shutdown) return;

        if (message == null) {
            logger.debug("Received a poison signal from the transport side, terminating all server side virtual channels on shared-channel {}", id);
            shutdown();
            return;
        }

        if (message.getChannelOperation() == MuonMessage.ChannelOperation.closed) {
            logger.debug("Received a channel op closed message, terminating all server side virtual channels on shared-channel {}", id);
            shutdown();
            return;
        }

        SharedChannelInboundMessage msg = codecs.decode(message.getPayload(), message.getContentType(), SharedChannelInboundMessage.class);

        ChannelConnection<MuonInboundMessage, MuonOutboundMessage> connection = getConnectionToProtocol(msg);

        cleanupResources(msg);

        connection.send(msg.getMessage());
    }

    private void cleanupResources(SharedChannelInboundMessage msg) {
      if (msg.getMessage().getChannelOperation() == MuonMessage.ChannelOperation.closed) {
        logger.debug("Virtual channel {} is closed, removing from active list on {}", msg.getChannelId(), id);
        localConnections.remove(msg.getChannelId());
      }
    }

    private ChannelConnection<MuonInboundMessage, MuonOutboundMessage> getConnectionToProtocol(SharedChannelInboundMessage msg) {
        ChannelConnection<MuonInboundMessage, MuonOutboundMessage> protocolConnection = localConnections.get(msg.getChannelId());

        if (protocolConnection == null) {
            logger.debug("Establishing new channel {} for {} over shared-channel :{}", msg.getChannelId(), msg.getMessage().getProtocol(), id);
            protocolConnection = stacks.openServerChannel(msg.getMessage().getProtocol());
            protocolConnection.receive(arg -> {

              Codecs.EncodingResult result;

                if (arg == null) {
                  logger.debug("Virtual channel {} on shared-channel {} has sent a null, not forwarding, shared-channel {} remains open", msg.getChannelId(), id);
                  //TODO, this is server to client, send a STEP=closed message?
                  return;
                } else {
                  SharedChannelOutboundMessage sharedMsg = new SharedChannelOutboundMessage(msg.getChannelId(), arg);
                  result = codecs.encode(sharedMsg, new String[]{"application/json"});
                }

                MuonOutboundMessage outMsg = MuonMessageBuilder.fromService(arg.getSourceServiceName())
                        .contentType(result.getContentType())
                        .payload(result.getPayload())
                        .protocol(SharedChannelServerStacks.PROTOCOL)
                        .step(SharedChannelServerStacks.STEP)
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
      localConnections.clear();
      shutdown = true;
      logger.debug("Shared channel is shut down {}", id);
    }
}
