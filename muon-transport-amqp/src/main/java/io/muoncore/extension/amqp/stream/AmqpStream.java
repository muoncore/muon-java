package io.muoncore.extension.amqp.stream;

import io.muoncore.MuonStreamGenerator;
import io.muoncore.codec.Codecs;
import io.muoncore.extension.amqp.AmqpQueues;
import io.muoncore.extension.amqp.stream.client.AmqpStreamClient;
import io.muoncore.extension.amqp.stream.server.AmqpStreamControl;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

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
 * The data queue can have on it:
 *  - DATA
 *      semantics - Subscriber.onNext
 *      a data message
 *  - ERROR
 *      semantics - Subscriber.onError
 *      an error was thrown by the publisher
 *      The client must then terminate it's side. The local subscription will be removed
 *  - COMPLETE
 *      semantics Subscriber.onComplete
 *      The publisher is finished.
 *      Server side resources will be closed, the resource queue should be cleaned up
 */
public class AmqpStream {

    private Logger log = Logger.getLogger(AmqpStream.class.getName());

    public static final String STREAM_COMMAND = "command";
    private AmqpQueues queues;
    private AmqpStreamControl streamControl;
    private String commandQueue;
    private Codecs codecs;

    private Executor spinner = Executors.newSingleThreadExecutor();

    List<AmqpStreamClient> streamClients = new ArrayList<AmqpStreamClient>();

    public AmqpStream(String serviceName, AmqpQueues queues, Codecs codecs) {
        this.queues = queues;
        this.commandQueue = serviceName + "_stream_control";
        this.codecs = codecs;
        streamControl = new AmqpStreamControl(queues, codecs);
        listenToControlQueue();
        monitorClientExpiry();
    }

    private void monitorClientExpiry() {
        spinner.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(1500);

                        for(AmqpStreamClient client: new ArrayList<AmqpStreamClient>(streamClients)) {
                            if (client.getLastSeenKeepAlive() + 3500 < System.currentTimeMillis()) {
                                expireClientConnection(client);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void expireClientConnection(AmqpStreamClient client) {
        streamClients.remove(client);
        log.warning("Connection to service " + client.getStreamName() +
                " has expired. The server did not send keep-alive.");

        client.onError(
                new IllegalStateException(
                        "Connection to service " +
                                client.getStreamName() +
                                " has expired. The server did not send keep-alive."));
    }

    private void listenToControlQueue() {
        queues.listenOnQueueEvent(commandQueue, Void.class, streamControl);
    }

    public void streamSource(String streamName, MuonStreamGenerator pub) {
        streamControl.getPublisherStreams().put(streamName, pub);
    }

    public <T> void subscribeToStream(String remoteServiceName, String streamName, Class<T> type, Map<String, String> params, Subscriber<T> subscriber) {

        String remoteCommandQueue = remoteServiceName + "_stream_control";

        log.fine("Subscribing to remote stream " + remoteCommandQueue + ":" + streamName);

        streamClients.add(new AmqpStreamClient<T>(
                remoteCommandQueue,
                streamName,
                params,
                subscriber,
                type,
                codecs,
                queues));
    }

    public List<String> getStreamNames() {
        List<String> streams = new ArrayList<String>();

        for(AmqpStreamClient cl: streamClients) {
            streams.add(cl.getStreamName());
        }

        return streams;
    }
}
