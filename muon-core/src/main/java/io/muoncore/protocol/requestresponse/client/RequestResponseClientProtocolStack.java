package io.muoncore.protocol.requestresponse.client;

import io.muoncore.channel.async.StandardAsyncChannel;
import io.muoncore.codec.CodecsSource;
import io.muoncore.future.MuonFuture;
import io.muoncore.protocol.ServiceConfigurationSource;
import io.muoncore.protocol.channelfuture.ChannelFutureAdapter;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.RequestMetaData;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.transport.TransportClientSource;

import java.net.URI;
import java.net.URISyntaxException;

public interface RequestResponseClientProtocolStack extends
        TransportClientSource, CodecsSource, ServiceConfigurationSource {

    default <X,R> MuonFuture<Response<R>> request(String uri, X payload, Class<R> responseType) throws URISyntaxException {
        return request(new URI(uri), payload, responseType);
    }

    default <X,R> MuonFuture<Response<R>> request(URI uri, X payload, Class<R> responseType) {
        return request(new Request<>(new RequestMetaData(uri.getPath(), "", uri.getHost()), payload), responseType);
    }

    default <X,R> MuonFuture<Response<R>> request(Request<X> event, Class<R> responseType) {
        StandardAsyncChannel<Request<X>, Response<R>> api2rrp = new StandardAsyncChannel<>();

        ChannelFutureAdapter<Response<R>, Request<X>> adapter =
                new ChannelFutureAdapter<>(api2rrp.left());

        new RequestResponseClientProtocol<>(
                event.getMetaData().getTargetService(),
                api2rrp.right(),
                getTransportClient().openClientChannel(),
                responseType,
                getCodecs());

        return adapter.request(event);
    }
}
