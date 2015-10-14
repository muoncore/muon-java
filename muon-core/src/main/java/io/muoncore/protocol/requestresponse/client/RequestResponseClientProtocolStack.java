package io.muoncore.protocol.requestresponse.client;

import io.muoncore.channel.async.StandardAsyncChannel;
import io.muoncore.codec.CodecsSource;
import io.muoncore.future.MuonFuture;
import io.muoncore.protocol.channelfuture.ChannelFutureAdapter;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.transport.TransportClientSource;

public interface RequestResponseClientProtocolStack extends
        TransportClientSource, CodecsSource {

    default <X> MuonFuture<Response> request(Request<X> event) {
        StandardAsyncChannel<Request<X>, Response> api2rrp = new StandardAsyncChannel<>();

        ChannelFutureAdapter<Response, Request<X>> adapter =
                new ChannelFutureAdapter<>(api2rrp.left());

        new RequestResponseClientProtocol<>(api2rrp.right(), getTransportClient().openClientChannel(), getCodecs());

        return adapter.request(event);
    }
}
