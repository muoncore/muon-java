package io.muoncore.api;

import io.muoncore.channel.ChannelConnection;
import org.reactivestreams.Publisher;
import reactor.rx.broadcast.Broadcaster;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Take a bidirectional channel and add request/ response semantics to
 * it at the code level.
 */
public class ChannelFutureAdapter<Receive, Send> {

    private ChannelConnection<Send, Receive> channelConnection;

    public ChannelFutureAdapter(ChannelConnection<Send, Receive> channelConnection) {
        this.channelConnection = channelConnection;
    }

    public MuonFuture<Receive> request(Send obj) {
        ChannelFuture<Receive> ret = new ChannelFuture<>();

        channelConnection.receive(ret::setData);
        channelConnection.send(obj);

        return ret;
    }

    class ChannelFuture<X> implements MuonFuture<X> {

        final CountDownLatch responseReceivedSignal = new CountDownLatch(1);

        private volatile boolean isDone;
        private X data;
        private boolean cancelled = false;
        private PromiseFunction<X> onFulfilled;
        private Broadcaster<X> broadcast;

        public void setData(X data) {
            if (isDone) return;

            this.data = data;
            if (onFulfilled != null) {
                onFulfilled.call(data);
            }
            if (broadcast != null) {
                broadcast.accept(data);
            }
            shutdown();
            responseReceivedSignal.countDown();
        }

        @Override
        public Publisher<X> toPublisher() {
            if (broadcast == null) {
                broadcast = Broadcaster.create();
            }
            return broadcast;
        }

        @Override
        public boolean cancel(boolean b) {
            if (isDone) return false;
            shutdown();
            isDone = true;
            cancelled = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return isDone;
        }

        @Override
        public void then(PromiseFunction<X> onFulfilled) {
            this.onFulfilled = onFulfilled;
            if (this.data != null) {
                onFulfilled.call(data);
            }
        }

        @Override
        public X get() throws InterruptedException, ExecutionException {
            try {
                responseReceivedSignal.await();
                return data;
            } finally {
                shutdown();
            }
        }

        @Override
        public X get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            try {
                responseReceivedSignal.await(l, timeUnit);
                return data;
            } finally {
                shutdown();
            }
        }

        private void shutdown() {
            if (isDone || isCancelled()) return;
            isDone = true;
            channelConnection.shutdown();
            if (broadcast != null) {
                broadcast.onComplete();
            }
        }
    }
}
