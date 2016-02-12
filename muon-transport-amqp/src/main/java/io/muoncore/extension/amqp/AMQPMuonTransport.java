package io.muoncore.extension.amqp;

import io.muoncore.Discovery;
import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.exception.NoSuchServiceException;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AMQPMuonTransport implements MuonTransport {

    public final static String HEADER_PROTOCOL = "PROTOCOL";
    public final static String HEADER_REPLY_TO = "REPLY_TO";
    public final static String HEADER_RECEIVE_QUEUE = "LISTEN_ON";
    public final static String HEADER_SOURCE_SERVICE = "SOURCE_SERVICE";

    private Logger log = Logger.getLogger(AMQPMuonTransport.class.getName());
    private String rabbitUrl;
    private List<AmqpChannel> channels;
    private ServiceQueue serviceQueue;
    private AmqpChannelFactory channelFactory;
    private Discovery discovery;

    public AMQPMuonTransport(
            String url,
            ServiceQueue serviceQueue,
            AmqpChannelFactory channelFactory) {
        channels = new ArrayList<>();
        this.channelFactory = channelFactory;
        this.rabbitUrl = url;
        this.serviceQueue = serviceQueue;

        log.info("Connecting to AMQP host at " + rabbitUrl);
    }

    @Override
    public void shutdown() {
        new ArrayList<>(channels).stream().forEach(AmqpChannel::shutdown);
        serviceQueue.shutdown();
    }

    @Override
    public ChannelConnection<TransportOutboundMessage, TransportInboundMessage> openClientChannel(String serviceName, String protocol) {

        if (!discovery.findService( svc -> svc.getIdentifier().equals(serviceName))
                .isPresent()) {
            throw new NoSuchServiceException(serviceName);
        }

        AmqpChannel channel = channelFactory.createChannel();

        channel.onShutdown(msg -> {
            channels.remove(channel);
        });

        channel.initiateHandshake(serviceName, protocol);
        channels.add(channel);
        Channel<TransportOutboundMessage, TransportInboundMessage> intermediate = Channels.channel("AMQPChannelExternal", "AMQPChannelInternal");

        Channels.connect(intermediate.right(), channel);

        return intermediate.left();
    }

    public void start(Discovery discovery, final ServerStacks serverStacks) {
        this.discovery = discovery;
        log.info("Booting up transport with stack " + serverStacks);
        serviceQueue.onHandshake( handshake -> {
            log.fine("opening new server channel with " + serverStacks);
            ChannelConnection<TransportInboundMessage, TransportOutboundMessage> connection =
                    serverStacks.openServerChannel(handshake.getProtocol());
            AmqpChannel channel = channelFactory.createChannel();
            channel.respondToHandshake(handshake);

            Channels.connect(channel, connection);

            channels.add(channel);
        });
    }

    @Override
    public String getUrlScheme() {
        return "amqp";
    }

    @Override
    public URI getLocalConnectionURI() {
        try {
            return new URI(rabbitUrl);
        } catch (URISyntaxException e) {
            throw new MuonTransportFailureException("Invalid URI is provided: " + rabbitUrl, e);
        }
    }

    public int getNumberOfActiveChannels() {
        return channels.size();
    }
}
