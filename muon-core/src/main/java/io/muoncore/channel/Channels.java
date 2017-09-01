package io.muoncore.channel;

import io.muoncore.channel.impl.StandardAsyncChannel;
import io.muoncore.channel.impl.TimeoutChannel;
import io.muoncore.channel.impl.WiretapChannel;
import io.muoncore.channel.impl.ZipChannel;
import io.muoncore.channel.support.Scheduler;
import io.muoncore.message.MuonMessage;
import io.muoncore.transport.client.RingBufferLocalDispatcher;
import io.muoncore.transport.client.TransportMessageDispatcher;
import reactor.Environment;

import reactor.core.config.DispatcherType;

import java.util.function.Function;

public class Channels {

    public static void shutdown() {
//        EVENT_DISPATCHER.shutdown();
//        WORK_DISPATCHER.shutdown();
    }

    public static ZipChannel zipChannel(String name) {
        return new ZipChannel(Dispatchers.dispatcher(), name);
    }

    /**
     * Create a channel that will issue a timeout to the left if no messages come from the right within the period.
     */
    public static TimeoutChannel timeout(Scheduler scheduler, long timeout) {
        return new TimeoutChannel(Dispatchers.dispatcher(), scheduler, timeout);
    }

    /**
     * Create a channel that permits wiretap on the events moving across it.
     */
    public static <X extends MuonMessage,Y extends MuonMessage> Channel<X, Y> wiretapChannel(TransportMessageDispatcher wiretapDispatch) {
        return new WiretapChannel<>(Dispatchers.dispatcher(), wiretapDispatch);
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
        return new StandardAsyncChannel<>(leftname, rightname, Dispatchers.dispatcher());
    }

    /**
     * A channel that expects to do significant work on one side of the channel, so is allocated a worker to allow async
     * and parallel processing without impacting overall event dispatch. Event order becomes non deterministic
     * and multiple events will be being dispatched concurrently.
     */
    public static <X,Y> Channel<X, Y> workerChannel(String leftname, String rightname) {
        return new StandardAsyncChannel<>(leftname, rightname, Dispatchers.poolDispatcher());
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
