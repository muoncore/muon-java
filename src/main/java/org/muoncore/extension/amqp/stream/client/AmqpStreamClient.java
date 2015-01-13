package org.muoncore.extension.amqp.stream.client;

import org.muoncore.Muon;
import org.muoncore.extension.amqp.AmqpQueues;
import org.muoncore.extension.amqp.stream.AmqpStream;
import org.muoncore.extension.amqp.stream.server.AmqpStreamControl;
import org.muoncore.transports.MuonMessageEvent;
import org.muoncore.transports.MuonMessageEventBuilder;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

public class AmqpStreamClient implements
        Muon.EventMessageTransportListener,
        Subscription {
    private AmqpQueues queues;
    private String privateStreamQueue;
    private String commandQueue;
    private Subscriber subscriber;

    private String remoteId;

    private Logger log = Logger.getLogger(AmqpStreamClient.class.getName());

    public AmqpStreamClient(String commandQueue, String streamName, Subscriber subscriber, AmqpQueues queues) {
        this.queues = queues;
        this.subscriber = subscriber;
        this.commandQueue = commandQueue;

        privateStreamQueue = UUID.randomUUID().toString();
        queues.listenOnQueueEvent(privateStreamQueue, this);

        queues.send(commandQueue,
                MuonMessageEventBuilder.named("")
                        .withNoContent()
                        .withHeader(AmqpStream.STREAM_COMMAND, AmqpStreamControl.COMMAND_SUBSCRIBE)
                        .withHeader(AmqpStreamControl.REQUESTED_STREAM_NAME, streamName)
                        .withHeader(AmqpStreamControl.REPLY_STREAM_NAME, privateStreamQueue).build());

    }

    @Override
    public void onEvent(String name, MuonMessageEvent obj) {
        if (obj.getHeaders().get(AmqpStream.STREAM_COMMAND) != null &&
                obj.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(AmqpStreamControl.SUBSCRIPTION_ACK)) {
            remoteId = obj.getHeaders().get(AmqpStreamControl.SUBSCRIPTION_STREAM_ID);
            log.fine("Received SUBSCRIPTION_ACK " + remoteId + " activating local subscription");
            subscriber.onSubscribe(this);
        } else if (obj.getHeaders().get("TYPE").equals("data")) {
            subscriber.onNext(obj.getPayload());
        } else if (obj.getHeaders().get("TYPE").equals("error")) {
            subscriber.onError(new IOException(obj.getHeaders().get("ERROR")));
        } else if (obj.getHeaders().get("TYPE").equals("complete")) {
            subscriber.onComplete();
        }
    }

    @Override
    public void request(long n) {
        //request the remote publisher to send more data
        log.finer("Requesting " + n + " more data from server " + remoteId);
        queues.send(commandQueue,
                MuonMessageEventBuilder.named("")
                        .withNoContent()
                        .withHeader(AmqpStream.STREAM_COMMAND, AmqpStreamControl.COMMAND_REQUEST)
                        .withHeader(AmqpStreamControl.REQUEST_COUNT, String.valueOf(n))
                        .withHeader(AmqpStreamControl.SUBSCRIPTION_STREAM_ID, remoteId).build());
    }

    @Override
    public void cancel() {
        //request the remote publisher to cancel the remote subscription and send
        //no more data.
        //TODO then clean up everything
        queues.send(commandQueue,
                MuonMessageEventBuilder.named("")
                        .withNoContent()
                        .withHeader(AmqpStream.STREAM_COMMAND, AmqpStreamControl.COMMAND_CANCEL)
                        .withHeader(AmqpStreamControl.SUBSCRIPTION_STREAM_ID, remoteId).build());
    }
}
