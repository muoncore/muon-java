package org.muoncore.extension.amqp.stream.server;

import org.muoncore.codec.Codecs;
import org.muoncore.extension.amqp.AmqpQueues;
import org.muoncore.transports.MuonMessageEvent;
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
    private Codecs codecs;

    AmqpProxySubscriber(
            String resourceQueue,
            AmqpQueues queues,
            Codecs codecs) {
        this.queues = queues;
        this.resourceQueue = resourceQueue;
        this.codecs = codecs;
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

        MuonMessageEvent msg = MuonMessageEventBuilder.named(resourceQueue)
                .withHeader("TYPE", "data")
                .withContent(o).build();

        msg.setContentType(codecs.getBinaryContentType(o.getClass()));
        msg.setEncodedBinaryContent(codecs.encodeToByte(o));

        queues.send(resourceQueue, msg);
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
