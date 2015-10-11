package io.muoncore.protocol.event.client;

import io.muoncore.DiscoverySource;
import io.muoncore.channel.async.StandardAsyncChannel;
import io.muoncore.future.MuonFuture;
import io.muoncore.protocol.channelfuture.ChannelFutureAdapter;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocol;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.transport.TransportClientSource;

public interface EventClientProtocolStack extends
        TransportClientSource, DiscoverySource {

    default <X> MuonFuture<Response> event(Event<X> event) {

        StandardAsyncChannel<Event<X>, Response> api2eventproto = new StandardAsyncChannel<>();
        StandardAsyncChannel<Request<X>, Response> event2rrp = new StandardAsyncChannel<>();

        ChannelFutureAdapter<Response, Event<X>> adapter =
                new ChannelFutureAdapter<>(api2eventproto.left());

        new EventClientProtocol<>(
                getDiscovery(),
                api2eventproto.right(),
                event2rrp.left());

        new RequestResponseClientProtocol<>(event2rrp.right(), getTransportClient().openClientChannel());

        return adapter.request(event);
    }
}
