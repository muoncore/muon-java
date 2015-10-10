package io.muoncore.channel;

public interface ChannelConnection<Outbound, Inbound> {
    void receive(ChannelFunction<Inbound> function);
    void send(Outbound message);

    @FunctionalInterface
    interface ChannelFunction<T> {
        void apply(T arg);
    }
}
