package io.muoncore.channel;

import io.muoncore.channel.async.StandardAsyncChannel;
import reactor.Environment;
import reactor.core.Dispatcher;

import java.util.function.Function;

public class Channels {

    static Dispatcher WORK_DISPATCHER = Environment.workDispatcher();

    public static <X,Y> Channel<X, Y> channel(String leftname, String rightname) {
        return new StandardAsyncChannel<>(leftname, rightname, Environment.sharedDispatcher());
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
