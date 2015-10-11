package io.muoncore.future;

import io.muoncore.channel.ChannelConnection;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChannelConnectionBackedFuture<X> implements MuonFuture<X> {

    private ChannelConnection<?, X> channelConnection;

    public ChannelConnectionBackedFuture(ChannelConnection<?, X> channelConnection) {
        this.channelConnection = channelConnection;
        //extract this concept from ChannelFutureAdapter
//        channelConnection.receive(
//
//        );
    }

    @Override
    public Publisher<X> toPublisher() {
        return null;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public X get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return get();
    }

    @Override
    public X get() {
        return null;
    }

}