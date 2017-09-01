package io.muoncore.channel.impl;

import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Dispatcher;
import io.muoncore.exception.MuonException;
import io.muoncore.message.MuonMessage;
import io.muoncore.transport.client.TransportMessageDispatcher;

public class WiretapChannel<GoingLeft extends MuonMessage, GoingRight extends MuonMessage> implements Channel<GoingLeft, GoingRight> {

    private ChannelConnection<GoingLeft, GoingRight> left;
    private ChannelConnection<GoingRight, GoingLeft> right;

    private ChannelConnection.ChannelFunction<GoingLeft> leftFunction;
    private ChannelConnection.ChannelFunction<GoingRight> rightFunction;

    public static boolean echoOut = false;

    public WiretapChannel(Dispatcher dispatcher, TransportMessageDispatcher transportMessageDispatcher) {

        String leftname = "left";
        String rightname = "right";

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
                    if (echoOut) System.out.println("WiretapChannel[" + leftname + " >>>>> " + rightname + "]: Sending " + msg + " to " + leftFunction);
                    transportMessageDispatcher.dispatch(msg);
                    leftFunction.apply(message); }
                        ,  Throwable::printStackTrace);
            }

            @Override
            public void shutdown() {
                leftFunction.apply(null);
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
                        System.out.println("WiretapChannel[" + leftname + " <<<< " + rightname + "]: " + msg + " to " + rightFunction);
                    transportMessageDispatcher.dispatch(msg);
                    rightFunction.apply(message);
                }, Throwable::printStackTrace);
            }

            @Override
            public void shutdown() {
                rightFunction.apply(null);
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
