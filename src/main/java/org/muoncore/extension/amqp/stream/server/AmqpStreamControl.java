package org.muoncore.extension.amqp.stream.server;

import org.muoncore.Muon;
import org.muoncore.extension.amqp.AmqpQueues;
import org.muoncore.extension.amqp.stream.AmqpStream;
import org.muoncore.transports.MuonMessageEvent;
import org.muoncore.transports.MuonMessageEventBuilder;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AmqpStreamControl implements Muon.EventMessageTransportListener {
    public static final String COMMAND_REQUEST = "REQUEST";
    public static final String COMMAND_SUBSCRIBE = "SUBSCRIBE";
    public static final String COMMAND_CANCEL = "CANCEL";
    public static final String REPLY_STREAM_NAME = "REPLY_STREAM_NAME";
    public static final String REQUESTED_STREAM_NAME = "REQUESTED_STREAM_NAME";
    public static final String SUBSCRIPTION_STREAM_ID = "SUBSCRIPTION_STREAM_ID";
    public static final String REQUEST_COUNT = "N";
    public static final String SUBSCRIPTION_ACK = "SUBSCRIPTION_ACK";
    private HashMap<String, Publisher> streams = new HashMap<String, Publisher>();
    private HashMap<String, AmqpProxySubscriber> subscriptions = new HashMap<String, AmqpProxySubscriber>();

    private AmqpQueues queues;

    public AmqpStreamControl(AmqpQueues queues) {
        this.queues = queues;
    }

    @Override
    public void onEvent(String name, MuonMessageEvent ev) {
        if (ev.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(COMMAND_SUBSCRIBE)) {
            createNewSubscription(ev);
        } else if (ev.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(COMMAND_REQUEST)) {
            requestData(ev);
        } else if (ev.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(COMMAND_CANCEL)) {
            cancelSubscription(ev);
        }
    }

    public Map<String, Publisher> getStreams() {
        return streams;
    }

    private void createNewSubscription(MuonMessageEvent ev) {
        //create a sub id
        String id = UUID.randomUUID().toString();

        String replyStreamName = ev.getHeaders().get(REPLY_STREAM_NAME);
        String requestedStreamName = ev.getHeaders().get(REQUESTED_STREAM_NAME);

        AmqpProxySubscriber subscriber = new AmqpProxySubscriber(replyStreamName, queues);

        Publisher pub = streams.get(requestedStreamName);
        pub.subscribe(subscriber);

        subscriptions.put(id, subscriber);

        //send the sub id back to origin over replyTo queue.
        queues.send(replyStreamName,
                MuonMessageEventBuilder.named("")
                        .withContent("")
                        .withHeader(AmqpStream.STREAM_COMMAND, SUBSCRIPTION_ACK)
                        .withHeader(AmqpStreamControl.SUBSCRIPTION_STREAM_ID, id).build());
    }

    private void requestData(MuonMessageEvent ev) {
        //lookup the sub
        String id = ev.getHeaders().get(SUBSCRIPTION_STREAM_ID);
        AmqpProxySubscriber sub = subscriptions.get(id);
        long n = Long.parseLong(ev.getHeaders().get(REQUEST_COUNT));

        sub.request(n);
    }

    private void cancelSubscription(MuonMessageEvent ev) {
        String id = ev.getHeaders().get(SUBSCRIPTION_STREAM_ID);
        AmqpProxySubscriber sub = subscriptions.get(id);
        sub.cancel();
    }
}
