package io.muoncore.protocol;

import io.muoncore.channel.ChannelConnection;

/*
 * workaround for inability to directly invoke lambdas from groovy without the dynamic coercion
 *
 * When performing spock argument capture, the actual lambda appears in a groovy context.
 *
 * Requiring this shim be used to convert and invoke in java.
 */
public class ChannelFunctionExecShim {

    private ChannelConnection.ChannelFunction function;

    public ChannelFunctionExecShim(ChannelConnection.ChannelFunction func) {
        this.function = func;
    }

    public void call(Object arg) {
        function.apply(arg);
    }
}
