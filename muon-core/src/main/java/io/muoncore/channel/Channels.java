package io.muoncore.channel;

import java.util.function.Function;

import io.muoncore.channel.async.StandardAsyncChannel;
import io.muoncore.channel.async.WiretapChannel;
import io.muoncore.transport.TransportMessage;
import io.muoncore.transport.client.RingBufferLocalDispatcher;
import io.muoncore.transport.client.TransportMessageDispatcher;
import reactor.Environment;
import reactor.core.Dispatcher;
import reactor.core.config.DispatcherType;

public class Channels {

    static Dispatcher WORK_DISPATCHER = Environment.newDispatcher(32768, 200, DispatcherType.THREAD_POOL_EXECUTOR);
    static Dispatcher EVENT_DISPATCHER = new RingBufferLocalDispatcher("channel", 32768);

    /**
     * Create a channel that permits wiretap on the events moving across it.
     */
    public static <X extends TransportMessage,Y extends TransportMessage> Channel<X, Y> wiretapChannel(TransportMessageDispatcher wiretapDispatch) {
        return new WiretapChannel<>(EVENT_DISPATCHER, wiretapDispatch);
    }

    /**
     * Create a channel that will perform async, in order dispatch between two processes.
     * @param leftname
     * @param rightname
     * @param <X>
     * @param <Y>
     * @return
     */
    public static <X,Y> Channel<X, Y> channel(String leftname, String rightname) {
        return new StandardAsyncChannel<>(leftname, rightname, EVENT_DISPATCHER);
    }

    /**
     * A channel that expects to do significant work on one side of the channel, so is allocated a worker to allow async
     * and parallel processing without impacting overall event dispatch. Event order becomes non deterministic
     * and multiple events will be being dispatched concurrently.
     */
    public static <X,Y> Channel<X, Y> workerChannel(String leftname, String rightname) {
        return new StandardAsyncChannel<>(leftname, rightname, WORK_DISPATCHER);
    }
    public static <X,Y> void connect(ChannelConnection<X, Y> right, ChannelConnection<Y, X> left) {
        assert right != null;
        assert left != null;
        left.receive(right::send);
        right.receive(left::send);
    }

    public static <LeftIn,LeftOut, RightIn, RightOut>
        void connectAndTransform(ChannelConnection<LeftOut, LeftIn> left,
                                 ChannelConnection<RightOut, RightIn> right,
                                 Function<LeftIn, RightOut> transformerLeftToRight,
                                 Function<RightIn, LeftOut> transformerRightToLeft) {

        left.receive( message -> {
            if (message == null) {
                right.shutdown();
                return;
            }
            right.send(transformerLeftToRight.apply(message));
        });
        right.receive(message -> {
            if (message == null) {
                left.shutdown();
                return;
            }
            left.send(transformerRightToLeft.apply(message));
        });
    }
}
