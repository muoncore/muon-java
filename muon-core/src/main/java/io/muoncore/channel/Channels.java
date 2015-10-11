package io.muoncore.channel;

import java.util.function.Function;

public class Channels {
    public static <X,Y> void connect(ChannelConnection<X, Y> right, ChannelConnection<Y, X> left) {
        left.receive(right::send);
        right.receive(left::send);
    }
    public static <LeftIn,LeftOut, RightIn, RightOut>
        void transform(ChannelConnection<LeftOut, LeftIn> left,
                                  ChannelConnection<RightOut, RightIn> right,
                                  Function<LeftIn, RightOut> transformerLeftToRight,
                                  Function<RightIn, LeftOut> transformerRightToLeft) {

        left.receive( message -> right.send(transformerLeftToRight.apply(message)));
        right.receive( message -> left.send(transformerRightToLeft.apply(message)));
    }
}
