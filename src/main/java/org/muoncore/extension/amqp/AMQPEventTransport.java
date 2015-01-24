package org.muoncore.extension.amqp;

import com.rabbitmq.client.*;
import org.muoncore.*;
import org.muoncore.extension.amqp.stream.AmqpStream;
import org.muoncore.transports.*;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AMQPEventTransport
        implements
        MuonQueueTransport,
        MuonResourceTransport,
        MuonBroadcastTransport,
        MuonStreamTransport {

    private Logger log = Logger.getLogger(AMQPEventTransport.class.getName());

    private Connection connection;
    private Channel channel;

    private String serviceName;

    private String rabbitUrl = "amqp://localhost:5672";

    private AmqpBroadcast broadcast;
    private AmqpDiscovery discovery;
    private AmqpResources resources;
    private AmqpQueues queues;
    private AmqpStream streams;

    public AMQPEventTransport(String serviceName) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException, IOException {
        this.serviceName = serviceName;

        String envRabbit = System.getenv("MUON_AMQP_URL");
        if (envRabbit != null && envRabbit.length() > 0) {
            rabbitUrl = envRabbit;
        }

        log.info("Connecting to AMQP host at " + rabbitUrl);
    }

    @Override
    public MuonService.MuonResult broadcast(String eventName, MuonMessageEvent event) {
        return broadcast.broadcast(eventName, event);
    }

    @Override
    public MuonService.MuonResult emitForReturn(String eventName, MuonResourceEvent event) {
        return resources.emitForReturn(eventName, event);
    }

    @Override
    public void listenOnResource(final String resource, final String verb, final Muon.EventResourceTransportListener listener) {
        resources.listenOnResource(resource, verb, listener);
    }

    @Override
    public void listenOnBroadcastEvent(final String resource, final Muon.EventMessageTransportListener listener) {
        broadcast.listenOnBroadcastEvent(resource, listener);
    }

    @Override
    public List<ServiceDescriptor> discoverServices() {
        return discovery.discoverServices();
    }

    @Override
    public void shutdown() {
        discovery.shutdown();
        queues.shutdown();
        resources.shutdown();
        broadcast.shutdown();

        try {
            channel.close();
            connection.close();
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void start() {
        ConnectionFactory factory = new ConnectionFactory();

        try {
            factory.setUri(rabbitUrl);
            connection = factory.newConnection();

            channel = connection.createChannel();

            broadcast = new AmqpBroadcast(channel);
            queues = new AmqpQueues(channel);
            resources = new AmqpResources(queues, serviceName);
            discovery = new AmqpDiscovery(serviceName, broadcast, this);
            streams = new AmqpStream(serviceName, queues);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MuonClient.MuonResult send(String queueName, MuonMessageEvent event) {
        return queues.send(queueName, event);
    }

    @Override
    public void listenOnQueueEvent(String queueName, Muon.EventMessageTransportListener listener) {
        queues.listenOnQueueEvent(queueName, listener);
    }

    @Override
    public void subscribeToStream(String url, Subscriber subscriber) throws URISyntaxException {
        URI uri = new URI(url);

        streams.subscribeToStream(uri.getHost(), uri.getPath(), subscriber);
    }

    @Override
    public void provideStreamSink(String streamName, Subscriber targetOfData) {

    }

    @Override
    public void provideStreamSource(String streamName, MuonStreamGenerator sourceOfData) {
        streams.streamSource(streamName, sourceOfData);
    }

    @Override
    public void publishToStream(String url, Publisher publisher) {

    }
}
