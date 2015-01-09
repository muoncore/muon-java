package org.muoncore.extension.amqp.stream;

import org.muoncore.Muon;
import org.muoncore.extension.amqp.AmqpQueues;
import org.muoncore.extension.amqp.stream.client.AmqpStreamClient;
import org.muoncore.extension.amqp.stream.server.AmqpStreamControl;
import org.muoncore.transports.MuonMessageEvent;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Every service has a stream control queue - servicename_stream_control
 * that queue has control messages
 *  - SUBSCRIBE (replyTo)
 *      new subscription to a stream. the service then replys on the given queue, which becomes the resource queue that the stream data is sent on
 *      sends reply SUB_ACTIVE(ID) with the ID of the susbcriber. This should be stored and used to control the subscription
 *      local implementation of Subscriber created, given to the
 *
 * - REQUEST (ID, N)
 *      remote is requesting N messages be sent. These go down the resource queue above
 *
 * - CANCEL (ID)
 *      remote is requesting that the subscription be closed.
 *      local resources destroyed, Subscribe
 *
 * The resource queue can then have on it:
 *  - DATA
 *      semantics - Subscriber.onNext
 *      a data message
 *  - ERROR
 *      semantics - Subscriber.onError
 *      an error was thrown by the publisher
 *  - COMPLETE
 *      semantics Subscriber.onComplete
 *      The publisher is finished.
 *      Server side resources will be closed, the resource queue should be cleaned up
 */
public class AmqpStream {

    public static final String STREAM_COMMAND = "command";
    private AmqpQueues queues;
    private AmqpStreamControl streamControl;
    private String commandQueue;

    //TODO, cleanups abound!
    List<AmqpStreamClient> streamClients = new ArrayList<AmqpStreamClient>();

    public AmqpStream(String serviceName, AmqpQueues queues) {
        this.queues = queues;
        this.commandQueue = serviceName + "_stream_control";
        streamControl = new AmqpStreamControl(queues);
        listenToControlQueue();
    }

    private void listenToControlQueue() {
        queues.listenOnQueueEvent(commandQueue, streamControl);
    }

    public void publishStream(String streamName, Publisher pub) {
        streamControl.getStreams().put(streamName, pub);
    }

    public void subscribeToStream(String remoteServiceName, String streamName, Subscriber subscriber) {

        String remoteCommandQueue = remoteServiceName + "_stream_control";

        System.out.println("Subscribing to remote stream " + remoteCommandQueue + ":" + streamName);

        streamClients.add(new AmqpStreamClient(
                remoteCommandQueue,
                streamName,
                subscriber,
                queues));
    }
}
