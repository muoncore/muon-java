package io.muoncore.transport.client;

import io.muoncore.Discovery;
import io.muoncore.channel.*;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.transport.*;
import io.muoncore.transport.sharedsocket.client.SharedSocketRoute;
import io.muoncore.transport.sharedsocket.client.SharedSocketRouter;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.function.Predicate;

/**
 * Transport layer control
 */
public class MultiTransportClient implements TransportClient, TransportControl {

    private List<MuonTransport> transports;
    private TransportMessageDispatcher taps;
    private Dispatcher dispatcher = Dispatchers.dispatcher();
    private AutoConfiguration configuration;
    private SharedSocketRouter sharedSocketRouter;
    private Discovery discovery;
    private TransportConnectionProvider transportConnectionProvider;

    public MultiTransportClient(
            List<MuonTransport> transports,
            TransportMessageDispatcher taps,
            AutoConfiguration config,
            Discovery discovery, Codecs codecs) {
        this.transports = transports;
        this.taps = taps;
        this.configuration = config;
        this.discovery = discovery;
        transportConnectionProvider = new DefaultTransportConnectionProvider(transports);
        this.sharedSocketRouter = new SharedSocketRouter((serviceName, onShutdown) -> {
            return new SharedSocketRoute(serviceName, transportConnectionProvider, codecs, configuration, onShutdown);
        });
    }

    @Override
    public ChannelConnection<MuonOutboundMessage, MuonInboundMessage> openClientChannel() {
        Channel<MuonOutboundMessage, MuonInboundMessage> tapChannel = Channels.wiretapChannel(taps);

        Channels.connect(
                tapChannel.right(),
                new MultiTransportClientChannelConnection(dispatcher, sharedSocketRouter, discovery, transportConnectionProvider, configuration));

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
