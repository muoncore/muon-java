package io.muoncore.channel.async;

import io.muoncore.channel.ChannelConnection;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

public class StandardAsyncChannelConnection<Outbound, Inbound> implements ChannelConnection<Outbound, Inbound> {

    private Optional<ChannelFunction<Inbound>> receiveFunction = Optional.empty();

    private LinkedBlockingQueue<Inbound> inbound;
    private LinkedBlockingQueue<Outbound> outbound;

    private ChannelWorker<Inbound> inboundWorker;

    public StandardAsyncChannelConnection(LinkedBlockingQueue<Inbound> inbound, LinkedBlockingQueue<Outbound> outbound) {
        this.inbound = inbound;
        this.outbound = outbound;
    }

    @Override
    public void receive(ChannelFunction<Inbound> function) {
        receiveFunction = Optional.of(function);
        inboundWorker = new ChannelWorker<>(inbound,
                data -> {
                    receiveFunction.ifPresent(func ->
                            func.apply(data)
                    );
                });

        inboundWorker.start();
    }

    @Override
    public void send(Outbound message) {
        outbound.add(message);
    }

    public class ChannelWorker<T> extends Thread {
        private final LinkedBlockingQueue<T> queue;
        private final ChannelFunction<T> function;

        public ChannelWorker(LinkedBlockingQueue<T> queue, ChannelFunction<T> function) {
            this.queue = queue;
            this.function = function;
        }

        public void run() {
            try {
                while ( true ) {
                    T s = queue.take();
                    function.apply(s);
                }
            }
            catch ( InterruptedException ie ) {
                System.out.print("FAILED");
            }
        }
    }
}
