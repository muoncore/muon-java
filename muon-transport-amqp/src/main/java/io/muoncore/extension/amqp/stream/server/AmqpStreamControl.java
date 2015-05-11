package io.muoncore.extension.amqp.stream.server;

import io.muoncore.Muon;
import io.muoncore.MuonStreamGenerator;
import io.muoncore.codec.Codecs;
import io.muoncore.extension.amqp.AmqpQueues;
import io.muoncore.extension.amqp.stream.AmqpStream;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.MuonMessageEventBuilder;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class AmqpStreamControl implements Muon.EventMessageTransportListener {
    private Logger log = Logger.getLogger(AmqpStream.class.getName());

    public static final String COMMAND_REQUEST = "REQUEST";
    public static final String COMMAND_KEEP_ALIVE = "KEEP-ALIVE";
    public static final String COMMAND_SUBSCRIBE = "SUBSCRIBE";
    public static final String COMMAND_CANCEL = "CANCEL";
    public static final String REPLY_QUEUE_NAME = "REPLY_QUEUE_NAME";
    public static final String REQUESTED_STREAM_NAME = "REQUESTED_STREAM_NAME";
    public static final String SUBSCRIPTION_STREAM_ID = "SUBSCRIPTION_STREAM_ID";
    public static final String REQUEST_COUNT = "N";
    public static final String SUBSCRIPTION_ACK = "SUBSCRIPTION_ACK";
    public static final String SUBSCRIPTION_NACK = "SUBSCRIPTION_NACK";
    private HashMap<String, MuonStreamGenerator> publisherStreams = new HashMap<String, MuonStreamGenerator>();
    private HashMap<String, AmqpProxySubscriber> subscriptions = new HashMap<String, AmqpProxySubscriber>();
    private ExecutorService spinner;

    private Map<String, Long> lastSeenKeepAlive = new HashMap<String, Long>();

    private AmqpQueues queues;
    private Codecs codecs;

    public AmqpStreamControl(final AmqpQueues queues, Codecs codecs) {
        this.queues = queues;
        this.codecs = codecs;
        spinner = Executors.newCachedThreadPool();
        monitorKeepAlive();
    }

    private void monitorKeepAlive() {
        final int KEEP_ALIVE_ERROR = 2000;
        //TODO, extract out into a monitor concept. This will vary hugely between transports.
        spinner.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(1000);
                        Set<String> subscriberIds = new HashSet<String>(subscriptions.keySet());
                        long expiryTime = System.currentTimeMillis() - KEEP_ALIVE_ERROR;
                        for (String subId : subscriberIds) {
                            if (lastSeenKeepAlive.get(subId) < expiryTime) {
                                harvestBrokenStream(subId);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void harvestBrokenStream(String id) {
        log.warning("Subscription " + id + " has timed out of keep-alive, expiring and closing");
        AmqpProxySubscriber subscriber = subscriptions.remove(id);
        lastSeenKeepAlive.remove(id);
        subscriber.onError(new IOException("No KEEP-ALIVE received within the required time period. This subscription is now closed"));
        subscriber.cancel();
    }

    @Override
    public void onEvent(String name, MuonMessageEvent ev) {
        if (ev.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(COMMAND_KEEP_ALIVE)) {
            lastSeenKeepAlive.put((String) ev.getHeaders().get(SUBSCRIPTION_STREAM_ID), System.currentTimeMillis());
        } else if (ev.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(COMMAND_SUBSCRIBE)) {
            createNewSubscription(ev);
        } else if (ev.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(COMMAND_REQUEST)) {
            requestData(ev);
        } else if (ev.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(COMMAND_CANCEL)) {
            cancelSubscription(ev);
        }
    }

    public Map<String, MuonStreamGenerator> getPublisherStreams() {
        return publisherStreams;
    }

    private void createNewSubscription(MuonMessageEvent ev) {
        //create a sub id
        String id = UUID.randomUUID().toString();

        String replyStreamName = (String) ev.getHeaders().get(REPLY_QUEUE_NAME);
        String requestedStreamName = (String) ev.getHeaders().get(REQUESTED_STREAM_NAME);

        AmqpProxySubscriber subscriber = new AmqpProxySubscriber(replyStreamName, queues, codecs);

        MuonStreamGenerator generator = publisherStreams.get(requestedStreamName);
        if (generator == null) {
            queues.send(replyStreamName,
                    MuonMessageEventBuilder.named("")
                            .withNoContent()
                            .withHeader(AmqpStream.STREAM_COMMAND, SUBSCRIPTION_NACK).build());
            return;
        }

        Publisher pub = generator.generatePublisher(ev.getHeaders());

        pub.subscribe(subscriber);

        lastSeenKeepAlive.put(id, System.currentTimeMillis());
        subscriptions.put(id, subscriber);

        //send the sub id back to origin over replyTo queue.
        queues.send(replyStreamName,
                MuonMessageEventBuilder.named("")
                        .withNoContent()
                        .withHeader(AmqpStream.STREAM_COMMAND, SUBSCRIPTION_ACK)
                        .withHeader(AmqpStreamControl.SUBSCRIPTION_STREAM_ID, id).build());
    }

    private void requestData(MuonMessageEvent ev) {
        //lookup the sub
        String id = (String) ev.getHeaders().get(SUBSCRIPTION_STREAM_ID);
        String replyQueue = (String) ev.getHeaders().get(REPLY_QUEUE_NAME);

        AmqpProxySubscriber sub = subscriptions.get(id);

        if (sub == null) {
            queues.send(replyQueue,
                    MuonMessageEventBuilder.named("")
                            .withNoContent()
                            .withHeader(AmqpStream.STREAM_COMMAND, "ERROR").build());
        } else {
            long n = Long.parseLong((String) ev.getHeaders().get(REQUEST_COUNT));
            sub.request(n);
        }
    }

    private void cancelSubscription(MuonMessageEvent ev) {
        String id = (String) ev.getHeaders().get(SUBSCRIPTION_STREAM_ID);
        lastSeenKeepAlive.remove(id);
        AmqpProxySubscriber sub = subscriptions.remove(id);
        if (sub != null){
            sub.cancel();
        }
    }
}
