package io.muoncore.channel;

import io.muoncore.channel.async.StandardAsyncChannel;
import reactor.Environment;
import reactor.core.Dispatcher;
import reactor.core.config.DispatcherType;

import java.util.function.Function;

public class Channels {

    static Dispatcher WORK_DISPATCHER = Environment.newDispatcher(32768, 200, DispatcherType.THREAD_POOL_EXECUTOR);
    static Dispatcher EVENT_DISPATCHER = Environment.newDispatcher(32768, 200, DispatcherType.RING_BUFFER);

    public static <X,Y> Channel<X, Y> channel(String leftname, String rightname) {
        return new StandardAsyncChannel<>(leftname, rightname, EVENT_DISPATCHER);
    }

    /**
     * A channel that expects to do significant work on one side of the channel, so is allocated a worker to allow async
     * processing without impacting overall event dispatch
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
