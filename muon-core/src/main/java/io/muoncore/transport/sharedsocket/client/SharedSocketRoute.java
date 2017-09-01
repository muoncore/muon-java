package io.muoncore.transport.sharedsocket.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.transport.client.TransportConnectionProvider;
import io.muoncore.transport.sharedsocket.client.messages.SharedChannelInboundMessage;
import io.muoncore.transport.sharedsocket.server.SharedChannelServerStacks;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages a single concrete muon socket to a remote service.
 *
 * Generates virtual sockets that it multiplexes along this shared socket.
 */
@Slf4j
public class SharedSocketRoute {

  private static final Logger logger = LoggerFactory.getLogger(SharedSocketRoute.class);

  private String serviceName;
    private ChannelConnection<MuonOutboundMessage, MuonInboundMessage> transportChannel;
    private final Map<String, SharedSocketChannelConnection> routes = new ConcurrentHashMap<>();

    private Codecs codecs;
    private AutoConfiguration configuration;
    private Runnable onShutdown;
    private boolean running = true;

    public SharedSocketRoute(String serviceName, TransportConnectionProvider transportConnectionProvider, Codecs codecs, AutoConfiguration configuration, Runnable onShutdown) {
        this.serviceName = serviceName;
        this.codecs = codecs;
        this.configuration = configuration;
        this.onShutdown = onShutdown;

        transportChannel = transportConnectionProvider.connectChannel(serviceName, SharedChannelServerStacks.PROTOCOL, inboundMessage -> {
            if (inboundMessage == null || inboundMessage.getChannelOperation() == MuonMessage.ChannelOperation.closed) {
              shutdownRoute(inboundMessage);
            } else {
              SharedChannelInboundMessage message = codecs.decode(inboundMessage.getPayload(), inboundMessage.getContentType(), SharedChannelInboundMessage.class);
              SharedSocketChannelConnection route = routes.get(message.getChannelId());
              route.sendInbound(message.getMessage());
            }
        });
        if (transportChannel == null) {
          throw new MuonTransportFailureException("Unable to construct a socket connection to " + serviceName);
        }
    }

    private void shutdownRoute(MuonInboundMessage msg) {
      if (running) {
        log.info("Shutting down shared-route due to channel failure {}", msg);
        routes.values().forEach(sharedSocketChannelConnection -> sharedSocketChannelConnection.sendInbound(msg));
        onShutdown.run();
        transportChannel.shutdown();
        running = false;
      }
    }

    /**
     * Open a channel over the shared route.
     **/
    public ChannelConnection<MuonOutboundMessage, MuonInboundMessage> openClientChannel() {

        SharedSocketChannelConnection ret = new SharedSocketChannelConnection(codecs, outboundMessage -> {

            Codecs.EncodingResult result = codecs.encode(outboundMessage, new String[] {"application/json"});

            MuonOutboundMessage out = MuonMessageBuilder.fromService(configuration.getServiceName())
                    .protocol(SharedChannelServerStacks.PROTOCOL)
                    .contentType(result.getContentType())
                    .payload(result.getPayload())
                    .step(SharedChannelServerStacks.STEP)
                    .toService(serviceName)
                    .build();

            transportChannel.send(out);
        }, () -> {
          //TODO, implement this as a specific step.
          //a client side channel shutdown is ignored by shared-channel.
        });

        routes.put(ret.getChannelId(), ret);

        return ret;
    }
}
