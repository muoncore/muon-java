package io.muoncore.protocol.channelfuture;

import io.muoncore.future.MuonFuture;
import io.muoncore.channel.ChannelConnection;
import org.reactivestreams.Publisher;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Take a bidirectional channel and add request/ response semantics to
 * it at the code level.
 *
 * There's a certain assumption that this is sitting on top of a more
 * intelligent request/ response aware protocol that can handle failure
 * however that isn't required if the channel is reliable.
 *
 * @param <Receive>
 * @param <Send>
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
        //TODO, closing the connection down once data flows or timeout reached, how?

        return ret;
    }

    static class ChannelFuture<X> implements MuonFuture<X> {

        final CountDownLatch responseReceivedSignal = new CountDownLatch(1);

        boolean isDone;
        private X data;

        public void setData(X data) {
            this.data = data;
            responseReceivedSignal.countDown();
        }

        @Override
        public Publisher<X> toPublisher() {
            throw new IllegalStateException("Not implemented for channel adapter");
        }

        @Override
        public boolean cancel(boolean b) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return isDone;
        }

        @Override
        public X get() throws InterruptedException, ExecutionException {
            responseReceivedSignal.await();
            return data;
        }

        @Override
        public X get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            responseReceivedSignal.await(l, timeUnit);
            return data;
        }
    }
}
