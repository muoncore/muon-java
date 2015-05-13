package io.muoncore.extension.amqp.stream.server;

import io.muoncore.codec.Codecs;
import io.muoncore.extension.amqp.AmqpQueues;
import io.muoncore.extension.amqp.stream.AmqpStream;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.MuonMessageEventBuilder;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Proxies original subscription on the other side of this MQ.
 */
public class AmqpProxySubscriber implements Subscriber {
    private Logger log = Logger.getLogger(AmqpProxySubscriber.class.getName());

    private Subscription subscription;
    private AmqpQueues queues;
    private String resourceQueue;
    private Codecs codecs;
    private ScheduledExecutorService keepAliveScheduler;

    AmqpProxySubscriber(
            final String resourceQueue,
            AmqpQueues queues,
            Codecs codecs) {
        this.queues = queues;
        this.resourceQueue = resourceQueue;
        this.codecs = codecs;
        keepAliveScheduler = Executors.newScheduledThreadPool(1);

        sendKeepAlive();
    }

    private void sendKeepAlive() {

        Runnable keepAliveSender = new Runnable() {
            @Override
            public void run() {
                log.info("Sending client keep-alive " + resourceQueue);
                MuonMessageEvent ev = MuonMessageEventBuilder.named(
                        "")
                        .withNoContent()
                        .withHeader(AmqpStream.STREAM_COMMAND, AmqpStreamControl.COMMAND_KEEP_ALIVE)
                        .build();

                AmqpProxySubscriber.this.queues.send(
                        resourceQueue,
                        ev);
            }
        };

        keepAliveScheduler.scheduleAtFixedRate(keepAliveSender, 0, 1, TimeUnit.SECONDS);
    }

    public void cancel() {
        log.info("Subscription cancelled");
        keepAliveScheduler.shutdownNow();
        if (subscription != null) {
            subscription.cancel();
        }
    }

    public void request(long n) {
        if (subscription != null) {
            subscription.request(n);
        }
    }

    @Override
    public void onSubscribe(Subscription s) {
        subscription = s;
    }

    @Override
    public void onNext(Object o) {

        MuonMessageEvent msg = MuonMessageEventBuilder.named(resourceQueue)
                .withHeader("TYPE", "data")
                .withContent(o).build();

        //TODO, include an accepts heqder in the subscription negotiation
        //use that to pick a content type up front and then use that
        //in all subsequent sends.
        msg.setContentType(codecs.getBinaryContentType(o.getClass()));
        msg.setEncodedBinaryContent(codecs.encodeToByte(o));

        queues.send(resourceQueue, msg);
    }

    @Override
    public void onError(Throwable t) {
        log.info("Subscription error");
        keepAliveScheduler.shutdownNow();

        queues.send(resourceQueue,
                MuonMessageEventBuilder.named(resourceQueue)
                        .withNoContent()
                        .withHeader("TYPE", "error")
                        .withHeader("ERROR", t.getMessage()).build());
    }

    @Override
    public void onComplete() {
        log.info("Subscription complete");
        keepAliveScheduler.shutdownNow();
        queues.send(resourceQueue,
                MuonMessageEventBuilder.named(resourceQueue)
                        .withNoContent()
                        .withHeader("TYPE", "complete").build());
    }
}
