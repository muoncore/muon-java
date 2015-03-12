package org.muoncore.extension.amqp;

import org.muoncore.*;
import org.muoncore.codec.Codecs;
import org.muoncore.codec.TransportCodecType;
import org.muoncore.extension.amqp.stream.AmqpStream;
import org.muoncore.transports.*;
import org.reactivestreams.Subscriber;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

public class AMQPEventTransport
        implements
        MuonQueueTransport,
        MuonResourceTransport,
        MuonBroadcastTransport,
        MuonStreamTransport {

    private Logger log = Logger.getLogger(AMQPEventTransport.class.getName());

    private String serviceName;
    private List<String> tags;

    private String rabbitUrl = "amqp://localhost:5672";

    private AmqpConnection connection;
    private AmqpBroadcast broadcast;
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
    public MuonService.MuonResult broadcast(String eventName, MuonMessageEvent event) {
        return broadcast.broadcast(eventName, event);
    }

    @Override
    public MuonService.MuonResult emitForReturn(String eventName, MuonResourceEvent event) {
        return resources.emitForReturn(eventName, event);
    }

    @Override
    public <T> void listenOnResource(String resource, String verb, Class<T> type, Muon.EventResourceTransportListener<T> listener) {
        resources.listenOnResource(resource, verb, listener);
    }

    @Override
    public void listenOnBroadcastEvent(final String resource, final Muon.EventMessageTransportListener listener) {
        broadcast.listenOnBroadcastEvent(resource, listener);
    }

    @Override
    public void shutdown() {
        queues.shutdown();
        resources.shutdown();
        broadcast.shutdown();
        connection.close();
    }

    public void start() {
        try {
            connection = new AmqpConnection(rabbitUrl);
            broadcast = new AmqpBroadcast(connection);
            queues = new AmqpQueues(connection.getChannel());
            resources = new AmqpResources(queues, serviceName, codecs);
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
    public void subscribeToStream(String url,Map<String,String> params, Subscriber subscriber) throws URISyntaxException {
        URI uri = new URI(url);

        streams.subscribeToStream(uri.getHost(), uri.getPath(), params, subscriber);
    }

    @Override
    public void provideStreamSource(String streamName, MuonStreamGenerator sourceOfData) {
        streams.streamSource(streamName, sourceOfData);
    }

    @Override
    public String getUrlScheme() {
        return "amqp";
    }

    @Override
    public URI getLocalConnectionURI() throws URISyntaxException {
        return new URI(rabbitUrl);
    }

    @Override
    public TransportCodecType getCodecType() {
        return TransportCodecType.BINARY;
    }
}
