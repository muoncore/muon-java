package io.muoncore.internal;

import io.muoncore.MuonFuture;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Allows the use of a Future interface, backed by an arbitrary publisher.
 */
public class PublisherBackedFuture<X> implements MuonFuture<X> {

    private Publisher<X> theReturn;
    private X capturedReturn;
    private Throwable capturedError;

    public PublisherBackedFuture(Publisher<X> val) {
        this.theReturn = val;
    }

    @Override
    public Publisher<X> toPublisher() {
        return theReturn;
    }

    @Override
    public X get() {

        final CountDownLatch responseReceivedSignal = getCountDownLatch();

        try {
            responseReceivedSignal.await();
            if (capturedError != null) {
                throw new RuntimeException(capturedError);
            }
            return capturedReturn;
        } catch (InterruptedException e) {
            throw new IllegalStateException("While waiting for resource return, interrupted");
        }
    }

    private CountDownLatch getCountDownLatch() {
        final CountDownLatch responseReceivedSignal = new CountDownLatch(1);

        theReturn.subscribe(new Subscriber<X>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(X x) {
                capturedReturn = x;
                responseReceivedSignal.countDown();
            }

            @Override
            public void onError(Throwable t) {
                capturedError = t;
                t.printStackTrace();
                responseReceivedSignal.countDown();
            }

            @Override
            public void onComplete() {
                responseReceivedSignal.countDown();
            }
        });
        return responseReceivedSignal;
    }

    @Override
    public X get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        final CountDownLatch responseReceivedSignal = getCountDownLatch();
        try {
            responseReceivedSignal.await(timeout, unit);
            if (capturedError != null) {
                throw new RuntimeException(capturedError);
            }
            return capturedReturn;
        } catch (InterruptedException e) {
            throw new IllegalStateException("While waiting for resource return, interrupted");
        }
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
        return capturedError != null || capturedReturn != null;
    }

}
