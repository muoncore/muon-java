package io.muoncore.transport.client;

import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.transport.*;
import org.reactivestreams.Publisher;
import reactor.Environment;
import reactor.core.Dispatcher;
import reactor.core.config.DispatcherType;

import java.util.function.Predicate;

/**
 * Transport layer bound to a single transport.
 */
public class SingleTransportClient implements TransportClient, TransportControl {

    private MuonTransport transport;
    private TransportMessageDispatcher taps;
//    private Dispatcher dispatcher = Environment.newDispatcher("transportDispatch", 8192);
    private Dispatcher dispatcher = Environment.newDispatcher(8192, 10, DispatcherType.RING_BUFFER);

    public SingleTransportClient(
            MuonTransport transport,
            TransportMessageDispatcher taps) {
        this.transport = transport;
        this.taps = taps;
    }

    @Override
    public ChannelConnection<TransportOutboundMessage, TransportInboundMessage> openClientChannel() {
        Channel<TransportOutboundMessage, TransportInboundMessage> tapChannel = Channels.wiretapChannel(taps);

        Channels.connect(
                tapChannel.right(),
                new SingleTransportClientChannelConnection(transport, dispatcher));

        return tapChannel.left();
    }

    @Override
    public void shutdown() {
        transport.shutdown();
        taps.shutdown();
    }

    @Override
    public Publisher<TransportMessage> tap(Predicate<TransportMessage> msg) {
        return taps.observe(msg);
    }
}
