package io.muoncore.transport.client;

import io.muoncore.channel.Dispatcher;
import io.muoncore.channel.Dispatchers;
import io.muoncore.message.MuonMessage;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;

public class SimpleTransportMessageDispatcher implements TransportMessageDispatcher {

    private List<QueuePredicate> queues = new ArrayList<>();
    private Dispatcher dispatcher = Dispatchers.dispatcher();

    private static final MuonMessage POISON = new MuonMessage(null, 0, null, null, null, null, null, null,null,null, null);

    @Override
    public void dispatch(MuonMessage message) {
        dispatcher.dispatch(message, m ->
                queues.stream().forEach(msg -> msg.add(m)), Throwable::printStackTrace);
    }

    @Override
    public void shutdown() {
        dispatch(POISON);
    }

    @Override
    public Publisher<MuonMessage> observe(Predicate<MuonMessage> filter) {

        LinkedBlockingQueue<MuonMessage> queue = new LinkedBlockingQueue<>();

        QueuePredicate wrapper = new QueuePredicate(queue, filter);
        queues.add(wrapper);

        return s -> s.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
                Dispatchers.poolDispatcher().dispatch(null, ev -> {
                    for (int i = 0; i < n; i++) {
                        try {
                            MuonMessage msg = queue.take();
                            if (msg == POISON) {
                                s.onComplete();
                                return;
                            } else {
                                s.onNext(msg);
                            }
                        } catch (InterruptedException e) {
                            s.onError(e);
                        }
                    }
                }, throwable -> {});
            }

            @Override
            public void cancel() {
                queues.remove(wrapper);
                queue.clear();
            }
        });
    }

    static class QueuePredicate {
        private Queue<MuonMessage> queue;
        private Predicate<MuonMessage> predicate;

        public QueuePredicate(Queue<MuonMessage> queue, Predicate<MuonMessage> predicate) {
            this.queue = queue;
            this.predicate = predicate;
        }

        public void add(MuonMessage msg) {
            if (msg == null) return;

            if (predicate.test(msg)) {
                queue.add(msg);
            }
        }
    }
}
