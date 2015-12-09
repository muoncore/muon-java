package io.muoncore.channel;

/**
 * Represents a step in a channel. A channel is a bidirectional communication stream between components.
 *
 * @param <Outbound> What will be sent to this channel
 * @param <Inbound> What will be received from this channel
 */
public interface ChannelConnection<Outbound, Inbound> {
    /**
     * Set the receiver function. Anything that comes out of this connection will be sent to the passed in function.
     *
     * Only one receiver function is active. If called more than once, minimal expectation is that the last function
     * is called, others will be ignored. Some ChannelConnections may throw an error in this case.
     */
    void receive(ChannelFunction<Inbound> function);

    /**
     * Send an event down this channel.
     *
     * This may be synchronous or asynchronous depending on the implementation.
     */
    void send(Outbound message);

    /**
     * Causes the channel this connection is fronting to shut itself down.
     * This will be via sending a special poison message down the channel.
     */
    void shutdown();

    @FunctionalInterface
    interface ChannelFunction<T> {
        void apply(T arg);
    }
}
