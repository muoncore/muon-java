package io.muoncore.protocol.event.client;

import io.muoncore.DiscoverySource;
import io.muoncore.ServiceDescriptor;
import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelFutureAdapter;
import io.muoncore.channel.Channels;
import io.muoncore.codec.CodecsSource;
import io.muoncore.config.MuonConfigurationSource;
import io.muoncore.exception.MuonException;
import io.muoncore.future.MuonFuture;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.reactivestream.client.ReactiveStreamClientProtocolStack;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocol;
import io.muoncore.transport.TransportClientSource;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;
import org.reactivestreams.Subscriber;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

public interface EventClientProtocolStack extends
        TransportClientSource, DiscoverySource, CodecsSource, MuonConfigurationSource, ReactiveStreamClientProtocolStack {

    /**
     * Listen to an event source stream, allowing the creation of an aggregated data structure (a reduction or projection)
     * Or serial processing of the stream.
     *
     * This method requires an event store to be active in the distributed system. If one is not active, a MuonException
     * will be thrown.
     *
     * This will replay from the start of the stream up to the current and then switch to HOT processing for all messages
     * after this.
     *
     * @param streamName The name of the stream to be replayed
     * @param subscriber The reactive streams subscriber that will listen to the event stream.
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException
     */
    default void replay(String streamName, EventReplayMode mode,  Subscriber<Event> subscriber) throws URISyntaxException, UnsupportedEncodingException {
        //TODO, introduce params.
        Optional<ServiceDescriptor> eventStore = getDiscovery().findService(svc -> svc.getTags().contains("eventstore"));
        if (eventStore.isPresent()) {
            String eventStoreName = eventStore.get().getIdentifier();
            subscribe(new URI("stream://" + eventStoreName + "/" + streamName), Event.class, subscriber);
        } else {
            throw new MuonException("There is no event store present in the distributed system");
        }
    }

    default <X> MuonFuture<Response<Map>> event(Event<X> event) {

        Channel<Event<X>, Response<Map>> api2eventproto = Channels.channel("eventapi", "eventproto");
        Channel<Request<Event<X>>, Response<Map>> event2rrp = Channels.channel("eventproto", "rrpproto");
        Channel<TransportOutboundMessage, TransportInboundMessage> rrp2transport = Channels.channel("rrpproto", "transport");

        ChannelFutureAdapter<Response<Map>, Event<X>> adapter =
                new ChannelFutureAdapter<>(api2eventproto.left());

        new EventClientProtocol<>(
                getConfiguration(),
                getDiscovery(),
                api2eventproto.right(),
                event2rrp.left());

        new RequestResponseClientProtocol<>(
                getConfiguration().getServiceName(),
                event2rrp.right(),
                rrp2transport.left(), Map.class, getCodecs());

        Channels.connectAndTransform(rrp2transport.right(), getTransportClient().openClientChannel(),
                transportOutboundMessage -> transportOutboundMessage.cloneWithProtocol("event"),
                transportInboundMessage -> transportInboundMessage);

        return adapter.request(event);
    }
}
