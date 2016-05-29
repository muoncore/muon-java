package io.muoncore.channel.impl;

import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.exception.MuonException;
import reactor.core.Dispatcher;

import java.util.Date;

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
                if (leftFunction == null) {
                    throw new MuonException("Other side of the channel [" + rightname + "] is not connected to receive data");
                }
                dispatcher.dispatch(message, msg -> {
                    if (echoOut) System.out.println(new Date() + ": Channel[" + leftname + " >>>>> " + rightname + "]: Sending " + msg + " to " + leftFunction);
                    leftFunction.apply(message); }
                        ,  Throwable::printStackTrace);
            }

            @Override
            public void shutdown() {
                dispatcher.dispatch(this, msg -> {
                    if (echoOut)
                        System.out.println(new Date() + ": Channel[" + rightname + " <<<< " + leftFunction+ "]: SHUTDOWN to " + leftFunction);
                    leftFunction.apply(null);
                }, Throwable::printStackTrace);
            }
        };

        right = new ChannelConnection<GoingRight, GoingLeft>() {
            @Override
            public void receive(ChannelFunction<GoingLeft> function) {
                leftFunction = function;
            }

            @Override
            public void send(GoingRight message) {
                if (rightFunction == null) {
                    throw new MuonException("Other side of the channel [" + rightname + "] is not connected to receive data");
                }
                dispatcher.dispatch(message, msg -> {
                    if (echoOut)
                        System.out.println(new Date() + ": Channel[" + leftname + " <<<< " + rightname + "]: " + msg + " to " + rightFunction);
                    rightFunction.apply(message);
                }, Throwable::printStackTrace);
            }

            @Override
            public void shutdown() {
                dispatcher.dispatch(this, msg -> {
                    if (echoOut)
                        System.out.println(new Date() + ": Channel[" + leftname + " <<<< " + rightname + "]: SHUTDOWN to " + rightFunction);
                    rightFunction.apply(null);
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
