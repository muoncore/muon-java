package io.muoncore.transport.client;

import io.muoncore.transport.TransportMessage;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;

public class SimpleTransportMessageDispatcher implements TransportMessageDispatcher {

    private List<Queue<TransportMessage>> queues = new ArrayList<>();
    private Executor exec = Executors.newFixedThreadPool(20);

    @Override
    public void dispatch(TransportMessage message) {
        queues.stream().forEach(msg -> msg.add(message));
    }

    @Override
    public Publisher<TransportMessage> observe(Predicate<TransportMessage> filter) {

        LinkedBlockingQueue<TransportMessage> queue = new LinkedBlockingQueue<>();
        queues.add(queue);

        return s -> s.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
                exec.execute(() -> {
                    for (int i = 0; i < n; i++) {
                        try {
                            s.onNext(queue.take());
                        } catch (InterruptedException e) {
                            s.onError(e);
                        }
                    }
                });
            }

            @Override
            public void cancel() {
                queues.remove(queue);
                queue.clear();
            }
        });
    }
}
