package io.muoncore.transport.client;

import java.util.function.Predicate;

import org.reactivestreams.Publisher;

import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportControl;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportMessage;
import io.muoncore.transport.TransportOutboundMessage;
import reactor.core.Dispatcher;

/**
 * Transport layer bound to a single transport.
 */
public class SingleTransportClient implements TransportClient, TransportControl {

    private MuonTransport transport;
    private TransportMessageDispatcher taps;
//    private Dispatcher dispatcher = Environment.newDispatcher("transportDispatch", 8192);
    private Dispatcher dispatcher = new RingBufferLocalDispatcher("transportDispatch", 8192);

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
