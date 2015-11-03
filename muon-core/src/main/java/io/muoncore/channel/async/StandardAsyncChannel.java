package io.muoncore.channel.async;

import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;

import java.util.concurrent.LinkedBlockingQueue;

public class StandardAsyncChannel<GoingLeft, GoingRight> implements Channel<GoingLeft, GoingRight> {

    private LinkedBlockingQueue<GoingLeft> rightToLeft = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<GoingRight> leftToRight = new LinkedBlockingQueue<>();

    private StandardAsyncChannelConnection<GoingLeft, GoingRight> left;
    private StandardAsyncChannelConnection<GoingRight, GoingLeft> right;

    public StandardAsyncChannel() {
        left = new StandardAsyncChannelConnection<>(leftToRight, rightToLeft);
        right = new StandardAsyncChannelConnection<>(rightToLeft, leftToRight);
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
