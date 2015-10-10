package io.muoncore.extension.amqp.stream;

import io.muoncore.MuonStreamGenerator;
import io.muoncore.crud.codec.Codecs;
import io.muoncore.extension.amqp.AmqpQueues;
import io.muoncore.extension.amqp.stream.client.AmqpStreamClient;
import io.muoncore.extension.amqp.stream.server.AmqpStreamControl;
import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

    private ScheduledExecutorService keepAliveExpiryScheduler = Executors.newScheduledThreadPool(1);

    List<AmqpStreamClient> streamClients = Collections.synchronizedList(new ArrayList<AmqpStreamClient>());

    public AmqpStream(String serviceName, AmqpQueues queues, Codecs codecs) {
        this.queues = queues;
        this.commandQueue = serviceName + "_stream_control";
        this.codecs = codecs;
        streamControl = new AmqpStreamControl(queues, codecs);
        listenToControlQueue();
        monitorClientExpiry();
    }

    private void monitorClientExpiry() {
        final int KEEP_ALIVE_EXPIRY = 20000;
        Runnable keepAliveSender = new Runnable() {
            @Override
            public void run() {
                List<AmqpStreamClient> clients = new ArrayList<AmqpStreamClient>(streamClients);
                for(AmqpStreamClient client: clients) {
                    if (System.currentTimeMillis() - client.getLastSeenKeepAlive() > KEEP_ALIVE_EXPIRY) {
                        log.info("Expiring connection, number of clients is :" + clients.size());
                        expireClientConnection(client);
                    }
                }
            }
        };

        keepAliveExpiryScheduler.scheduleAtFixedRate(keepAliveSender, 4, 2, TimeUnit.SECONDS);
    }

    private void expireClientConnection(AmqpStreamClient client) {
        streamClients.remove(client);

        if (client.isTerminated()) {
            log.info("Connection to service " + client.getStreamName() +
                    " has terminated. Removing local record");
            return;
        }

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

        log.info("Subscribing to remote stream " + remoteCommandQueue + ":" + streamName);

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
