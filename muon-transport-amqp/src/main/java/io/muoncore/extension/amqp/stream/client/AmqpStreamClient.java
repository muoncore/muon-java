package io.muoncore.extension.amqp.stream.client;

import io.muoncore.Muon;
import io.muoncore.codec.Codecs;
import io.muoncore.extension.amqp.AmqpQueues;
import io.muoncore.extension.amqp.stream.AmqpStream;
import io.muoncore.extension.amqp.stream.server.AmqpStreamControl;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.MuonMessageEventBuilder;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class AmqpStreamClient<T> implements
        Muon.EventMessageTransportListener,
        Subscription {
    private AmqpQueues queues;
    private String streamName;
    private String privateStreamQueue;
    private String commandQueue;
    private Subscriber<T> subscriber;
    private Class<T> type;
    private Codecs codecs;

    private String remoteId;

    private ExecutorService spinner;
    boolean running = false;

    private Logger log = Logger.getLogger(AmqpStreamClient.class.getName());

    private long lastSeenKeepAlive;

    public AmqpStreamClient(
            String commandQueue,
            String streamName,
            Map<String, String> params,
            Subscriber<T> subscriber,
            Class<T> type,
            Codecs codecs,
            AmqpQueues queues) {
        this.queues = queues;
        this.subscriber = subscriber;
        this.commandQueue = commandQueue;
        this.streamName = streamName;
        this.type = type;
        this.codecs = codecs;

        privateStreamQueue = UUID.randomUUID().toString();
        queues.listenOnQueueEvent(privateStreamQueue, Void.class, this);


        MuonMessageEvent ev = MuonMessageEventBuilder.named("")
                .withNoContent()
                .withHeader(AmqpStream.STREAM_COMMAND, AmqpStreamControl.COMMAND_SUBSCRIBE)
                .withHeader(AmqpStreamControl.REQUESTED_STREAM_NAME, streamName)
                .withHeader(AmqpStreamControl.REPLY_QUEUE_NAME, privateStreamQueue).build();

        ev.getHeaders().putAll(params);
        //TODO, necessary?
        ev.setContentType("application/json");
        queues.send(commandQueue, ev);

        lastSeenKeepAlive = System.currentTimeMillis() + 3000;
    }

    public String getStreamName() {
        return streamName;
    }

    @Override
    public void onEvent(String name, MuonMessageEvent obj) {
        if (obj.getHeaders().get(AmqpStream.STREAM_COMMAND) != null &&
                obj.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(AmqpStreamControl.SUBSCRIPTION_ACK)) {
            remoteId = (String) obj.getHeaders().get(AmqpStreamControl.SUBSCRIPTION_STREAM_ID);
            log.fine("Received SUBSCRIPTION_ACK " + remoteId + " activating local subscription");
            lastSeenKeepAlive = System.currentTimeMillis();
            sendKeepAlive();
            subscriber.onSubscribe(this);
        } else if (obj.getHeaders().get(AmqpStream.STREAM_COMMAND) != null &&
                    obj.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(AmqpStreamControl.SUBSCRIPTION_NACK)) {
            log.warning("SUBSCRIPTION_NACK for stream [" + streamName + "] stream is NOT established");
            subscriber.onError(new IllegalArgumentException("SUBSCRIPTION_NACK for stream [" + streamName + "] stream is NOT established"));
        } else if (obj.getHeaders().get(AmqpStream.STREAM_COMMAND) != null &&
                obj.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(AmqpStreamControl.COMMAND_KEEP_ALIVE)) {
            lastSeenKeepAlive = System.currentTimeMillis();
        } else if (obj.getHeaders().get("TYPE").equals("data")) {
            byte[] data = obj.getBinaryEncodedContent();
            T decodedObject = codecs.decodeObject(data, obj.getContentType(), type);
            subscriber.onNext(decodedObject);
        } else if (obj.getHeaders().get("TYPE").equals("error")) {
            shutdown();
            onError(new IOException((String) obj.getHeaders().get("ERROR")));
        } else if (obj.getHeaders().get("TYPE").equals("complete")) {
            shutdown();
            subscriber.onComplete();
        } else {
            log.warning("Received an unknown message on the control channel " + obj.getHeaders());
        }
    }

    public void onError(Exception ex) {
        shutdown();
        subscriber.onError(ex);
    }

    public long getLastSeenKeepAlive() {
        return lastSeenKeepAlive;
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

    private void sendKeepAlive() {
        if (running) return;
        if (spinner != null) {
            spinner.shutdownNow();
        }
        spinner = Executors.newSingleThreadExecutor();
        spinner.execute(new Runnable() {
            @Override
            public void run() {
                running = true;
                while(running) {
                    try {
                        log.fine("Sending client keep alive : " + remoteId);
                        MuonMessageEvent ev = MuonMessageEventBuilder.named(
                                "")
                                .withNoContent()
                                .withHeader(AmqpStreamControl.SUBSCRIPTION_STREAM_ID, remoteId)
                                .withHeader(AmqpStream.STREAM_COMMAND, AmqpStreamControl.COMMAND_KEEP_ALIVE)
                                .build();

                        AmqpStreamClient.this.queues.send(
                                commandQueue,
                                ev);
                        Thread.sleep(1000);
                    } catch(InterruptedException ex) {
                        running = false;
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void shutdown() {
        running=false;
        if (spinner != null) {
            spinner.shutdownNow();
            spinner = null;
        }
        queues.removeListener(this);
    }

    @Override
    public void cancel() {
        shutdown();
        queues.send(commandQueue,
                MuonMessageEventBuilder.named("")
                        .withNoContent()
                        .withHeader(AmqpStream.STREAM_COMMAND, AmqpStreamControl.COMMAND_CANCEL)
                        .withHeader(AmqpStreamControl.SUBSCRIPTION_STREAM_ID, remoteId).build());
        log.info("Subscription " + remoteId + " has been cancelled, the remote has been notified");
    }
}
