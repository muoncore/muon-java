package io.muoncore.protocol.event.client;

import io.muoncore.DiscoverySource;
import io.muoncore.channel.Channels;
import io.muoncore.channel.async.StandardAsyncChannel;
import io.muoncore.codec.CodecsSource;
import io.muoncore.future.MuonFuture;
import io.muoncore.protocol.channelfuture.ChannelFutureAdapter;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocol;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.transport.TransportClientSource;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

public interface EventClientProtocolStack extends
        TransportClientSource, DiscoverySource, CodecsSource {

    default <X> MuonFuture<Response> event(Event<X> event) {

        StandardAsyncChannel<Event<X>, Response> api2eventproto = new StandardAsyncChannel<>();
        StandardAsyncChannel<Request<X>, Response> event2rrp = new StandardAsyncChannel<>();
        StandardAsyncChannel<TransportOutboundMessage, TransportInboundMessage> rrp2transport = new StandardAsyncChannel<>();

        ChannelFutureAdapter<Response, Event<X>> adapter =
                new ChannelFutureAdapter<>(api2eventproto.left());

        new EventClientProtocol<>(
                getDiscovery(),
                api2eventproto.right(),
                event2rrp.left());

        new RequestResponseClientProtocol<>(event2rrp.right(), rrp2transport.left(), getCodecs());

        Channels.connectAndTransform(rrp2transport.right(), getTransportClient().openClientChannel(),
                transportOutboundMessage -> transportOutboundMessage.cloneWithProtocol("event"),
                transportInboundMessage -> transportInboundMessage);

        return adapter.request(event);
    }
}
