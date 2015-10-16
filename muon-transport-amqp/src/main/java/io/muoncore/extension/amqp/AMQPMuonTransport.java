package io.muoncore.extension.amqp;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class AMQPMuonTransport implements MuonTransport {

    public final static String HEADER_PROTOCOL = "PROTOCOL";
    public final static String HEADER_REPLY_TO = "REPLY_TO";
    public final static String HEADER_SOURCE_SERVICE = "SOURCE_SERVICE";

    private Logger log = Logger.getLogger(AMQPMuonTransport.class.getName());
    private String rabbitUrl;
    private String localServiceName;
    private AmqpConnection connection;
    private ServerStacks serverStacks;
    private List<AmqpChannel> channels;
    private ServiceQueue serviceQueue;
    private AmqpChannelFactory channelFactory;

    public AMQPMuonTransport(
            String url,
            String localServiceName,
            ServerStacks stacks,
            ServiceQueue serviceQueue,
            AmqpChannelFactory channelFactory) {
        channels = new ArrayList<>();
        this.channelFactory = channelFactory;
        this.rabbitUrl = url;
        this.localServiceName = localServiceName;
        this.serverStacks = stacks;
        this.serviceQueue = serviceQueue;

        log.info("Connecting to AMQP host at " + rabbitUrl);
    }

    @Override
    public void shutdown() {
        connection.close();
    }

    @Override
    public ChannelConnection<TransportOutboundMessage, TransportInboundMessage> openClientChannel(String serviceName, String protocol) {
        AmqpChannel channel = channelFactory.createChannel();
        channel.initiateHandshake(serviceName, protocol);
        channels.add(channel);
        return channel;
    }

    public void start() {
        serviceQueue.onHandshake( handshake -> {
            AmqpChannel channel = channelFactory.createChannel();
            channel.respondToHandshake(handshake);

            Channels.connect(channel,
            serverStacks.openServerChannel(handshake.getProtocol()));

            channels.add(channel);
        });
    }

    @Override
    public String getUrlScheme() {
        return "amqp";
    }

    @Override
    public URI getLocalConnectionURI() throws URISyntaxException {
        return new URI(rabbitUrl);
    }
}
