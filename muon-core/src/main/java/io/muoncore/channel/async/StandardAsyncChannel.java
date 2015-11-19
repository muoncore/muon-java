package io.muoncore.channel.async;

import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import reactor.Environment;
import reactor.core.Dispatcher;

public class StandardAsyncChannel<GoingLeft, GoingRight> implements Channel<GoingLeft, GoingRight> {

    private Dispatcher dispatcher = Environment.sharedDispatcher();

    private ChannelConnection<GoingLeft, GoingRight> left;
    private ChannelConnection<GoingRight, GoingLeft> right;

    private ChannelConnection.ChannelFunction<GoingLeft> leftFunction;
    private ChannelConnection.ChannelFunction<GoingRight> rightFunction;

    public StandardAsyncChannel() {
        left = new ChannelConnection<GoingLeft, GoingRight>() {
            @Override
            public void receive(ChannelFunction<GoingRight> function) {
                rightFunction = function;
            }

            @Override
            public void send(GoingLeft message) {
                dispatcher.dispatch(message, msg -> leftFunction.apply(message), er -> System.out.println("Failed!!"));
            }
        };

        right = new ChannelConnection<GoingRight, GoingLeft>() {
            @Override
            public void receive(ChannelFunction<GoingLeft> function) {
                leftFunction = function;
            }

            @Override
            public void send(GoingRight message) {
                dispatcher.dispatch(message, msg -> rightFunction.apply(message), er -> System.out.println("Failed!!"));
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
