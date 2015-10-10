package io.muoncore.channel;


/**
 * Terminology (left/ right) assumes you draw pictures. Go draw.
 * @param <GoingRight> Data moving right along the channel
 * @param <GoingLeft> Data moving left along the channel.
 */
public interface Channel<GoingRight, GoingLeft> {
    ChannelConnection<GoingLeft, GoingRight> right();
    ChannelConnection<GoingRight, GoingLeft> left();
}
