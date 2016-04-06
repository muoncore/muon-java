package io.muoncore.protocol.event.client;

import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.ServiceDescriptor;
import io.muoncore.api.ChannelFutureAdapter;
import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.descriptors.ServiceExtendedDescriptor;
import io.muoncore.exception.MuonException;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.EventProtocolMessages;
import io.muoncore.protocol.reactivestream.client.ReactiveStreamClientProtocolStack;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;
import io.muoncore.transport.client.TransportClient;
import org.reactivestreams.Subscriber;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class DefaultEventClient implements EventClient {

    private AutoConfiguration config;
    private Discovery discovery;
    private Codecs codecs;
    private TransportClient transportClient;
    private ReactiveStreamClientProtocolStack reactiveStreamClientProtocolStack;

    private ChannelConnection<TransportOutboundMessage, TransportInboundMessage> eventChannelConnection;

    private boolean useEventProtocol = false;
    private Muon muon;

    public DefaultEventClient(Muon muon) {
        this.muon = muon;
        this.config = muon.getConfiguration();
        this.discovery = muon.getDiscovery();
        this.codecs = muon.getCodecs();
        this.transportClient = muon.getTransportClient();
        this.reactiveStreamClientProtocolStack = muon;


        detectEventEmitProtocol(muon);
    }

    /**
     * Use either RPC or dedicated event emit protocol, if available.
     *
     * @param muon
     */
    private void detectEventEmitProtocol(Muon muon) {
        Optional<ServiceDescriptor> eventStore = discovery.findService(svc -> svc.getTags().contains("eventstore"));
        if (!eventStore.isPresent()) {
            throw new MuonException("Unable to locate an event store in the distirbuted system. Is Photon running?");
        }
        try {
            ServiceExtendedDescriptor descriptor = muon.introspect(eventStore.get().getIdentifier()).get();
            Optional<ProtocolDescriptor> event =
                    descriptor.getProtocols().stream().filter(
                            it -> it.getProtocolScheme().equals(EventProtocolMessages.PROTOCOL)).findAny();
            if (event.isPresent()) {
                useEventProtocol = true;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new MuonException(e);
        }
    }

//    @Override
//    public <X> MuonFuture<EventNode> loadChain(String eventId) {
//        return null;
//    }
//
//    @Override
//    public <X> MuonFuture<Event<X>> loadEvent(String id, Class<X> type) {
//        return null;
//    }

    @Override
    public EventResult event(String streamName, Event event) {
        try {
            if (useEventProtocol) {
                return emitUsingEventProtocol(streamName, event);
            }
            return emitUsingLegacyRpcProtocol(streamName, event);
        } catch (InterruptedException | ExecutionException e) {
            throw new MuonException(e);
        }
    }

    private EventResult emitUsingLegacyRpcProtocol(String streamName, Event event) throws ExecutionException, InterruptedException {
        //simulate the Event structure for the rpc usage.
        Map<String, Object> payload = new HashMap<>();
        payload.put("stream-name", streamName);
        payload.put("payload", event.getPayload());
        payload.put("eventType", event.getEventType());
        payload.put("parentId", event.getParentId());
        payload.put("serviceId", event.getServiceId());
        payload.put("id", event.getId());

        Response<Map> resp = muon.request("request://photon/events", payload, Map.class).get();
        if (resp.getStatus() == 200) {
            return new EventResult(EventResult.EventResultStatus.PERSISTED, "Persisted");
        }
        return new EventResult(EventResult.EventResultStatus.FAILED, (String) resp.getPayload().get("message"));
    }

    private <X> EventResult emitUsingEventProtocol(String streamName, Event<X> event) throws ExecutionException, InterruptedException {
        Channel<Event<X>, EventResult> api2eventproto = Channels.channel("eventapi", "eventproto");
        Channel<TransportOutboundMessage, TransportInboundMessage> rrp2transport = Channels.channel("eventproto", "transport");

        ChannelFutureAdapter<EventResult, Event<X>> adapter =
                new ChannelFutureAdapter<>(api2eventproto.left());

        new EventClientProtocol<>(
                streamName,
                config,
                discovery,
                codecs,
                api2eventproto.right(),
                rrp2transport.left());

        Channels.connect(rrp2transport.right(), transportClient.openClientChannel());

        return adapter.request(event).get();
    }

    @Override
    public void replay(String streamName, EventReplayMode mode, Subscriber<Event> subscriber) {

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
                        Map.class, subscriber);
            } catch (URISyntaxException e) {
                throw new MuonException("The name provided [" + eventStoreName + "] is invalid");
            }
        } else {
            throw new MuonException("There is no event store present in the distributed system");
        }
    }

//    @Override
//    public <X> MuonFuture<EventProjection<X>> lookupProjection(String name, Type type) {
//        return null;
//    }

}
