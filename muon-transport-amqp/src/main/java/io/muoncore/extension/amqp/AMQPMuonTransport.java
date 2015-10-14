package io.muoncore.extension.amqp;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class AMQPMuonTransport implements MuonTransport {

    private Logger log = Logger.getLogger(AMQPMuonTransport.class.getName());
    private String rabbitUrl;
    private String localServiceName;
    private AmqpConnection connection;
    private ServerStacks serverStacks;

    public AMQPMuonTransport(String url, String localServiceName, ServerStacks stacks) {
        this.rabbitUrl = url;
        this.localServiceName = localServiceName;
        this.serverStacks = stacks;

        log.info("Connecting to AMQP host at " + rabbitUrl);
    }

    @Override
    public void shutdown() {
//        queues.shutdown();
//        resources.shutdown();
        connection.close();
    }

    @Override
    public ChannelConnection<TransportOutboundMessage, TransportInboundMessage> openClientChannel(String serviceName, String protocol) {
        return null;
    }

    public void start() {
        try {
            connection = new AmqpConnection(rabbitUrl);
//            queues = new AmqpQueues(connection.getChannel());
//            resources = new AmqpResources(queues, serviceName, codecs);
//            streams = new AmqpStream(serviceName, queues, codecs);
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
