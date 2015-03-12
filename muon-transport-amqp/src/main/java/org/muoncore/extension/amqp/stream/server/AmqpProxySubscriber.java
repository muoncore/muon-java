package org.muoncore.extension.amqp.stream.server;

import org.muoncore.extension.amqp.AmqpQueues;
import org.muoncore.transports.MuonMessageEventBuilder;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Proxies original subscription on the other side of this MQ.
 */
public class AmqpProxySubscriber implements Subscriber {
    private Subscription subscription;
    private AmqpQueues queues;
    private String resourceQueue;

    AmqpProxySubscriber(String resourceQueue, AmqpQueues queues) {
        this.queues = queues;
        this.resourceQueue = resourceQueue;
    }

    public void cancel() {
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
        queues.send(resourceQueue,
                MuonMessageEventBuilder.named(resourceQueue)
                        .withHeader("TYPE", "data")
                        .withContent(o).build());
    }

    @Override
    public void onError(Throwable t) {
        queues.send(resourceQueue,
                MuonMessageEventBuilder.named(resourceQueue)
                        .withNoContent()
                        .withHeader("TYPE", "error")
                        .withHeader("ERROR", t.getMessage()).build());
    }

    @Override
    public void onComplete() {
        queues.send(resourceQueue,
                MuonMessageEventBuilder.named(resourceQueue)
                        .withNoContent()
                        .withHeader("TYPE", "complete").build());
    }
}
