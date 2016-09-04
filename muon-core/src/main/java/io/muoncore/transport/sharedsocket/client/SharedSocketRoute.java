package io.muoncore.transport.sharedsocket.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.transport.client.TransportConnectionProvider;
import io.muoncore.transport.sharedsocket.client.messages.SharedChannelInboundMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages a single concrete muon socket to a remote service.
 *
 * Generates virtual sockets that it multiplexes along this shared socket.
 */
public class SharedSocketRoute {

    private static final Logger logger = LoggerFactory.getLogger(SharedSocketRoute.class);

    private String serviceName;
    private ChannelConnection<MuonOutboundMessage, MuonInboundMessage> sharedSocketConnection;
    private TransportConnectionProvider transportConnectionProvider;
    private Map<String, SharedSocketChannelConnection> routes = new HashMap<>();

    private Codecs codecs;
    private AutoConfiguration configuration;

    //TODO, list of channels over this route.
    //TODO, shutdown behaviour?

    public SharedSocketRoute(String serviceName, TransportConnectionProvider transportConnectionProvider, Codecs codecs, AutoConfiguration configuration) {
        this.serviceName = serviceName;
        this.codecs = codecs;
        this.configuration = configuration;

        sharedSocketConnection = transportConnectionProvider.connectChannel(serviceName, "shared-channel", inboundMessage -> {
//            if (inboundMessage == null) {
//                logger.warn("SharedRouter received a null message from the transport layer. This is not expected and will be dropped");
//                return;
//            }
            SharedChannelInboundMessage message = codecs.decode(inboundMessage.getPayload(), inboundMessage.getContentType(), SharedChannelInboundMessage.class);
            SharedSocketChannelConnection route = routes.get(message.getChannelId());
            route.sendInbound(message.getMessage());
        });
    }

    /**
     * Open a channel over the shared route.
     **/
    public ChannelConnection<MuonOutboundMessage, MuonInboundMessage> openClientChannel() {

        SharedSocketChannelConnection ret = new SharedSocketChannelConnection(codecs, outboundMessage -> {

            Codecs.EncodingResult result = codecs.encode(outboundMessage, new String[] {"application/json"});

            MuonOutboundMessage out = MuonMessageBuilder.fromService(configuration.getServiceName())
                    .protocol(SharedSocketRouter.PROTOCOL)
                    .contentType(result.getContentType())
                    .payload(result.getPayload())
                    .step("message")
                    .toService(serviceName)
                    .build();

            sharedSocketConnection.send(out);
        });

        routes.put(ret.getChannelId(), ret);

        return ret;
    }
}
