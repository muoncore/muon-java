package io.muoncore.protocol.event.client;

import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.ServiceDescriptor;
import io.muoncore.api.ChannelFutureAdapter;
import io.muoncore.api.MuonFuture;
import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.exception.MuonException;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.reactivestream.client.ReactiveStreamClientProtocolStack;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;
import io.muoncore.transport.client.TransportClient;
import org.reactivestreams.Subscriber;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class DefaultEventClient implements EventClient {

    private AutoConfiguration config;
    private Discovery discovery;
    private Codecs codecs;
    private TransportClient transportClient;
    private ReactiveStreamClientProtocolStack reactiveStreamClientProtocolStack;

    private ChannelConnection<TransportOutboundMessage, TransportInboundMessage> eventChannelConnection;

    public DefaultEventClient(Muon muon) {
        this.config = muon.getConfiguration();
        this.discovery = muon.getDiscovery();
        this.codecs = muon.getCodecs();
        this.transportClient = muon.getTransportClient();
        this.reactiveStreamClientProtocolStack = muon;
    }

    @Override
    public <X> MuonFuture<Event<X>> loadEvent(String id, Class<X> type) {
        return null;
    }

    @Override
    public <X> MuonFuture<EventResult> event(Event<X> event) {

        Channel<Event<X>, EventResult> api2eventproto = Channels.channel("eventapi", "eventproto");
        Channel<TransportOutboundMessage, TransportInboundMessage> rrp2transport = Channels.channel("eventproto", "transport");

        ChannelFutureAdapter<EventResult, Event<X>> adapter =
                new ChannelFutureAdapter<>(api2eventproto.left());

        new EventClientProtocol<>(
                config,
                discovery,
                codecs,
                api2eventproto.right(),
                rrp2transport.left());

        Channels.connect(rrp2transport.right(), transportClient.openClientChannel());

        return adapter.request(event);
    }

    @Override
    public void replay(String streamName, EventReplayMode mode, Subscriber<Event> subscriber) {

//TODO, introduce params.
        Optional<ServiceDescriptor> eventStore = discovery.findService(svc -> svc.getTags().contains("eventstore"));
        if (eventStore.isPresent()) {
            String eventStoreName = eventStore.get().getIdentifier();
            try {
                reactiveStreamClientProtocolStack.subscribe(new URI("stream://" + eventStoreName + "/" + streamName), Event.class, subscriber);
            } catch (URISyntaxException  e) {
                throw new MuonException("The name provided [" + eventStoreName + "] is invalid");
            }
        } else {
            throw new MuonException("There is no event store present in the distributed system");
        }
    }

    @Override
    public <X> MuonFuture<EventProjection<X>> lookupProjection(String name, Type type) {
        return null;
    }

}
