package io.muoncore.extension.amqp.stream.server;

import io.muoncore.codec.Codecs;
import io.muoncore.extension.amqp.AmqpQueues;
import io.muoncore.extension.amqp.stream.AmqpStream;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.MuonMessageEventBuilder;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Proxies original subscription on the other side of this MQ.
 */
public class AmqpProxySubscriber implements Subscriber {
    private Subscription subscription;
    private AmqpQueues queues;
    private String resourceQueue;
    private Codecs codecs;
    private ExecutorService spinner;

    private boolean running = true;

    AmqpProxySubscriber(
            final String resourceQueue,
            AmqpQueues queues,
            Codecs codecs) {
        this.queues = queues;
        this.resourceQueue = resourceQueue;
        this.codecs = codecs;
        spinner = Executors.newCachedThreadPool();

        sendKeepAlive();
    }

    private void sendKeepAlive() {
        spinner.execute(new Runnable() {
            @Override
            public void run() {
                while(running) {
                    try {
                        Thread.sleep(1000);
                        MuonMessageEvent ev = MuonMessageEventBuilder.named(
                                "")
                                .withNoContent()
                                .withHeader(AmqpStream.STREAM_COMMAND, AmqpStreamControl.COMMAND_KEEP_ALIVE)
                                .build();

                        AmqpProxySubscriber.this.queues.send(
                                resourceQueue,
                                ev);
                    } catch(InterruptedException ex) {
                        running = false;
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }


    public void cancel() {
        running=false;
        spinner.shutdownNow();
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
        running=false;
        spinner.shutdownNow();

        queues.send(resourceQueue,
                MuonMessageEventBuilder.named(resourceQueue)
                        .withNoContent()
                        .withHeader("TYPE", "error")
                        .withHeader("ERROR", t.getMessage()).build());
    }

    @Override
    public void onComplete() {
        running=false;
        spinner.shutdownNow();
        queues.send(resourceQueue,
                MuonMessageEventBuilder.named(resourceQueue)
                        .withNoContent()
                        .withHeader("TYPE", "complete").build());
    }
}
