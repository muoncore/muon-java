package io.muoncore.protocol.requestresponse.client;

import io.muoncore.channel.async.StandardAsyncChannel;
import io.muoncore.codec.CodecsSource;
import io.muoncore.future.MuonFuture;
import io.muoncore.protocol.ServiceConfigurationSource;
import io.muoncore.protocol.channelfuture.ChannelFutureAdapter;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.transport.TransportClientSource;

public interface RequestResponseClientProtocolStack extends
        TransportClientSource, CodecsSource, ServiceConfigurationSource {

    default <X,R> MuonFuture<Response<R>> request(Request<X> event, Class<R> responseType) {
        StandardAsyncChannel<Request<X>, Response<R>> api2rrp = new StandardAsyncChannel<>();

        ChannelFutureAdapter<Response<R>, Request<X>> adapter =
                new ChannelFutureAdapter<>(api2rrp.left());

        new RequestResponseClientProtocol<>(
                getConfiguration().getServiceName(),
                api2rrp.right(),
                getTransportClient().openClientChannel(),
                responseType,
                getCodecs());

        return adapter.request(event);
    }
}
