package io.muoncore.protocol.event.client;

import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.ServiceDescriptor;
import io.muoncore.api.ChannelFutureAdapter;
import io.muoncore.api.ImmediateReturnFuture;
import io.muoncore.api.MuonFuture;
import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.exception.MuonException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.reactivestream.client.ReactiveStreamClientProtocolStack;
import io.muoncore.transport.client.TransportClient;
import org.reactivestreams.Subscriber;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DefaultEventClient implements EventClient {

    private AutoConfiguration config;
    private Discovery discovery;
    private Codecs codecs;
    private TransportClient transportClient;
    private ReactiveStreamClientProtocolStack reactiveStreamClientProtocolStack;

    private ChannelConnection<MuonOutboundMessage, MuonInboundMessage> eventChannelConnection;
    private Muon muon;

    public DefaultEventClient(Muon muon) {
        this.muon = muon;
        this.config = muon.getConfiguration();
        this.discovery = muon.getDiscovery();
        this.codecs = muon.getCodecs();
        this.transportClient = muon.getTransportClient();
        this.reactiveStreamClientProtocolStack = muon;
    }

    @Override
    public <X> EventResult event(ClientEvent<X> event) {
        try {
            Channel<ClientEvent<X>, EventResult> api2eventproto = Channels.channel("eventapi", "eventproto");
            Channel<MuonOutboundMessage, MuonInboundMessage> rrp2transport = Channels.channel("eventproto", "transport");

            ChannelFutureAdapter<EventResult, ClientEvent<X>> adapter =
                    new ChannelFutureAdapter<>(api2eventproto.left());

            new EventClientProtocol<>(
                    config,
                    discovery,
                    codecs,
                    api2eventproto.right(),
                    rrp2transport.left());

            Channels.connect(rrp2transport.right(), transportClient.openClientChannel());

            return adapter.request(event).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new MuonException(e);
        }
    }

    @Override
    public <X> MuonFuture<EventReplayControl> replay(String streamName, EventReplayMode mode, Class<X> payloadType, Subscriber<Event<X>> subscriber) {

        String replayType;
        if (mode == EventReplayMode.LIVE_ONLY) {
            replayType = "hot";
        } else {
            replayType = "hot-cold";
        }

        Optional<ServiceDescriptor> eventStore = discovery.findService(svc -> svc.getTags().contains("eventstore"));
        if (eventStore.isPresent()) {
            String eventStoreName = eventStore.get().getIdentifier();
            try {
                reactiveStreamClientProtocolStack.subscribe(new URI("stream://" + eventStoreName + "/stream?stream-type=" + replayType + "&stream-name=" + streamName),
                        new EventParameterizedType(payloadType), subscriber);
            } catch (URISyntaxException e) {
                throw new MuonException("The name provided [" + eventStoreName + "] is invalid");
            }
        } else {
            throw new MuonException("There is no event store present in the distributed system, is Photon running?");
        }

        return null;
    }

    @Override
    public <X> MuonFuture<EventProjectionControl<X>> getProjection(String name, Class<X> type) {

        return new ImmediateReturnFuture<>(() -> {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("projection-name", name);
                return muon.request("request://photon/projection", data).get().getPayload(type);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public MuonFuture<List<EventProjectionDescriptor>> getProjectionList() {
        try {

            List<String> projectionKeys = (List<String>) muon.request("request://photon/projection-keys").get().getPayload(Map.class).get("projection-keys");

            List<EventProjectionDescriptor> projections = projectionKeys.stream().map(EventProjectionDescriptor::new).collect(Collectors.toList());

            return new ImmediateReturnFuture<>(projections);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
