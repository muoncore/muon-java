package io.muoncore.extension.amqp;

import io.muoncore.Discovery;
import io.muoncore.ServiceDescriptor;
import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.channel.impl.KeepAliveChannel;
import io.muoncore.channel.support.Scheduler;
import io.muoncore.codec.Codecs;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.exception.NoSuchServiceException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.transport.MuonTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AMQPMuonTransport implements MuonTransport {

    private Logger log = LoggerFactory.getLogger(AMQPMuonTransport.class.getName());
    private String rabbitUrl;
    private List<AmqpChannel> channels;
    private ServiceQueue serviceQueue;
    private AmqpChannelFactory channelFactory;
    private Discovery discovery;
    private Codecs codecs;
    private Scheduler scheduler;

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
    public boolean canConnectToService(String name) {
        Optional<ServiceDescriptor> descriptor = discovery.getServiceNamed(name);

        return descriptor.map(serviceDescriptor -> serviceDescriptor.getSchemes()
          .stream()
          .anyMatch(url -> url.equals(getUrlScheme())))
          .orElse(false);
    }

    @Override
    public ChannelConnection<MuonOutboundMessage, MuonInboundMessage> openClientChannel(String serviceName, String protocol) {

      log.info("Opening a channel ... {}", serviceName);
        if (!discovery.getServiceNamed(serviceName)
                .isPresent()) {
            throw new NoSuchServiceException(serviceName);
        }

        AmqpChannel channel = channelFactory.createChannel();

        channel.onShutdown(msg -> {
            channels.remove(channel);
        });

        channel.initiateHandshake(serviceName, protocol);
        channels.add(channel);
        Channel<MuonOutboundMessage, MuonInboundMessage> intermediate = new KeepAliveChannel(Channels.EVENT_DISPATCHER, protocol, scheduler);

        Channels.connect(intermediate.right(), channel);

        return intermediate.left();
    }

    public void start(Discovery discovery, final ServerStacks serverStacks, Codecs codecs, Scheduler scheduler) {
        this.discovery = discovery;
        this.codecs = codecs;
        this.scheduler = scheduler;

        channelFactory.initialiseEnvironment(codecs, discovery, scheduler);
        log.info("Booting up transport with stack " + serverStacks);
        serviceQueue.onHandshake( handshake -> {
            log.debug("opening new server channel with " + serverStacks);

            ChannelConnection<MuonInboundMessage, MuonOutboundMessage> serverChannelConnection =
                    serverStacks.openServerChannel(handshake.getProtocol());

            Channel<MuonOutboundMessage, MuonInboundMessage> keepAliveChannel =
                    new KeepAliveChannel(Channels.EVENT_DISPATCHER, handshake.getProtocol(), scheduler);

            AmqpChannel amqpChannel = channelFactory.createChannel();

            Channels.connect(amqpChannel, keepAliveChannel.right());
            Channels.connect(serverChannelConnection, keepAliveChannel.left());

            amqpChannel.respondToHandshake(handshake);


            channels.add(amqpChannel);
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
