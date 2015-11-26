package io.muoncore.channel.async;

import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.exception.MuonException;
import reactor.core.Dispatcher;

public class StandardAsyncChannel<GoingLeft, GoingRight> implements Channel<GoingLeft, GoingRight> {

    private Dispatcher dispatcher;

    private ChannelConnection<GoingLeft, GoingRight> left;
    private ChannelConnection<GoingRight, GoingLeft> right;

    private ChannelConnection.ChannelFunction<GoingLeft> leftFunction;
    private ChannelConnection.ChannelFunction<GoingRight> rightFunction;

    public static boolean echoOut = false;

    public StandardAsyncChannel(String leftname, String rightname, Dispatcher dispatcher) {

        this.dispatcher = dispatcher;

        left = new ChannelConnection<GoingLeft, GoingRight>() {
            @Override
            public void receive(ChannelFunction<GoingRight> function) {
                rightFunction = function;
            }

            @Override
            public void send(GoingLeft message) {
                if (message == null) {
                    throw new MuonException("Cannot dispatch null down a channel from " + rightname + " to " + leftname + ". Null is a bad idea");
                }
                dispatcher.dispatch(message, msg -> {
                    if (echoOut) System.out.println("Channel[" + leftname + " >>>>> " + rightname + "]: Sending " + msg + " to " + leftFunction);
                    leftFunction.apply(message); }
                        ,  Throwable::printStackTrace);
            }
        };

        right = new ChannelConnection<GoingRight, GoingLeft>() {
            @Override
            public void receive(ChannelFunction<GoingLeft> function) {
                leftFunction = function;
            }

            @Override
            public void send(GoingRight message) {
                if (message == null) {
                    throw new MuonException("Cannot dispatch null down a channel from " + leftname + " to " + rightname + ". Null is a bad idea");
                }
                dispatcher.dispatch(message, msg -> {
                    if (echoOut)
                        System.out.println("Channel[" + leftname + " <<<< " + rightname + "]: " + msg + " to " + rightFunction);
                    rightFunction.apply(message);
                }, Throwable::printStackTrace);
            }
        };
    }

    @Override
    public ChannelConnection<GoingRight, GoingLeft> right() {
        return right;
    }

    @Override
    public ChannelConnection<GoingLeft, GoingRight> left() {
        return left;
    }
}
