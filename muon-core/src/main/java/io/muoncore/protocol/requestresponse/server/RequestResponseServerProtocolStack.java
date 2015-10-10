package io.muoncore.protocol.requestresponse.server;

import io.muoncore.channel.async.StandardAsyncChannel;
import io.muoncore.future.MuonFuture;
import io.muoncore.protocol.channelfuture.ChannelFutureAdapter;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocol;
import io.muoncore.transport.TransportClientSource;

public interface RequestResponseServerProtocolStack extends
        TransportClientSource {

    default <X> MuonFuture<Response> handleRequest(Request<X> event) {
        StandardAsyncChannel<Request<X>, Response> api2rrp = new StandardAsyncChannel<>();

        ChannelFutureAdapter<Response, Request<X>> adapter =
                new ChannelFutureAdapter<>(api2rrp.left());

        new RequestResponseServerProtocol<>(api2rrp.right(), getTransportClient().openClientChannel());

        return adapter.request(event);
    }
}
