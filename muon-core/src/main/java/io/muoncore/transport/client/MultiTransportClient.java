package io.muoncore.transport.client;

import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.transport.*;
import org.reactivestreams.Publisher;
import reactor.core.Dispatcher;

import java.util.List;
import java.util.function.Predicate;

/**
 * Transport layer bound to a single transport.
 */
public class MultiTransportClient implements TransportClient, TransportControl {

    private List<MuonTransport> transports;
    private TransportMessageDispatcher taps;
    private Dispatcher dispatcher = new RingBufferLocalDispatcher("transportDispatch", 8192);

    public MultiTransportClient(
            List<MuonTransport> transports,
            TransportMessageDispatcher taps) {
        this.transports = transports;
        this.taps = taps;
    }

    @Override
    public ChannelConnection<MuonOutboundMessage, MuonInboundMessage> openClientChannel() {
        Channel<MuonOutboundMessage, MuonInboundMessage> tapChannel = Channels.wiretapChannel(taps);

        Channels.connect(
                tapChannel.right(),
                new MultiTransportClientChannelConnection(transports, dispatcher));

        return tapChannel.left();
    }

    @Override
    public void shutdown() {
        for (MuonTransport transport: transports) {
            transport.shutdown();
        }
        taps.shutdown();
    }

    @Override
    public Publisher<MuonMessage> tap(Predicate<MuonMessage> msg) {
        return taps.observe(msg);
    }
}
