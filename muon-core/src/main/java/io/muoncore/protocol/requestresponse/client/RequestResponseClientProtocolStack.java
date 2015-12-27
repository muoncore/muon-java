package io.muoncore.protocol.requestresponse.client;

import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelFutureAdapter;
import io.muoncore.channel.Channels;
import io.muoncore.codec.CodecsSource;
import io.muoncore.exception.MuonException;
import io.muoncore.future.MuonFuture;
import io.muoncore.protocol.ServiceConfigurationSource;
import io.muoncore.protocol.requestresponse.RRPTransformers;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.RequestMetaData;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.protocol.support.ProtocolTimerSource;
import io.muoncore.transport.TransportClientSource;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;

public interface RequestResponseClientProtocolStack extends
        TransportClientSource, CodecsSource, ServiceConfigurationSource, ProtocolTimerSource {

    default <R> MuonFuture<Response<R>> request(String uri, Class<R> responseType) {
        return request(uri, (Type) responseType);
    }

    default <R> MuonFuture<Response<R>> request(String uri, Type responseType) {
        return request(uri, new Object(), responseType);
    }

    default <X,R> MuonFuture<Response<R>> request(String uri, X payload, Class<R> responseType) {
        return request(uri, payload, (Type) responseType);
    }

    default <X,R> MuonFuture<Response<R>> request(String uri, X payload, Type responseType) {
        try {
            return request(new URI(uri), payload, responseType);
        } catch (URISyntaxException ex) {
            throw new MuonException("URI is incorrect", ex);
        }
    }

    default <X,R> MuonFuture<Response<R>> request(URI uri, X payload, Class<R> responseType) {
        return request(uri, payload, (Type) responseType);
    }

    default <X,R> MuonFuture<Response<R>> request(URI uri, X payload, Type responseType) {
        if (!uri.getScheme().equals(RRPTransformers.REQUEST_RESPONSE_PROTOCOL)) {
            throw new MuonException("Scheme is invalid: " + uri.getScheme() + ", requires scheme: " + RRPTransformers.REQUEST_RESPONSE_PROTOCOL);
        }
        return request(new Request<>(new RequestMetaData(uri.getPath(), getConfiguration().getServiceName(), uri.getHost()), payload), responseType);
    }

    default <X,R> MuonFuture<Response<R>> request(Request<X> event, Class<R> responseType) {
        return request(event, (Type) responseType);
    }

    default <X,R> MuonFuture<Response<R>> request(Request<X> event, Type responseType) {

        Channel<Request<X>, Response<R>> api2rrp = Channels.channel("rrpclientapi", "rrpclientproto");

        ChannelFutureAdapter<Response<R>, Request<X>> adapter =
                new ChannelFutureAdapter<>(api2rrp.left());

        new RequestResponseClientProtocol<>(
                getConfiguration().getServiceName(),
                api2rrp.right(),
                getTransportClient().openClientChannel(),
                responseType,
                getCodecs(),
                getProtocolTimer());

        return adapter.request(event);
    }
}
