package io.muoncore.extension.amqp;

import io.muoncore.crud.codec.Codecs;
import io.muoncore.extension.amqp.stream.AmqpStream;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

public class AMQPEventTransport
        implements
        MuonTransport {

    private Logger log = Logger.getLogger(AMQPEventTransport.class.getName());

    private String serviceName;
    private List<String> tags;

    private String rabbitUrl;

    private AmqpConnection connection;
    private AmqpResources resources;
    private AmqpQueues queues;
    private AmqpStream streams;
    private Codecs codecs;

    public AMQPEventTransport(
            String url,
            String serviceName,
            List<String> tags,
            Codecs codecs) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException, IOException {
        this.serviceName = serviceName;
        this.tags = tags;
        this.rabbitUrl = url;
        this.codecs = codecs;

        log.info("Connecting to AMQP host at " + rabbitUrl);
    }

    @Override
    public ChannelConnection<TransportOutboundMessage, TransportInboundMessage> inboundChannel(String name) {
        throw new IllegalStateException("Failed!");
    }

    @Override
    public ChannelConnection<TransportOutboundMessage, TransportInboundMessage> channelToRemote(String remoteName, String channelName) {
        throw new IllegalStateException("Failed!");
    }

    @Override
    public void shutdown() {
        queues.shutdown();
        resources.shutdown();
        connection.close();
    }

    public void start() {
        try {
            connection = new AmqpConnection(rabbitUrl);
            queues = new AmqpQueues(connection.getChannel());
            resources = new AmqpResources(queues, serviceName, codecs);
            streams = new AmqpStream(serviceName, queues, codecs);
        } catch (URISyntaxException | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
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
