package io.muoncore.protocol.requestresponse.client;

import io.muoncore.api.ChannelFutureAdapter;
import io.muoncore.api.MuonFuture;
import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.channel.support.SchedulerSource;
import io.muoncore.codec.CodecsSource;
import io.muoncore.exception.MuonException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServiceConfigurationSource;
import io.muoncore.protocol.requestresponse.RRPTransformers;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.transport.TransportClientSource;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @deprecated in favour of {@link io.muoncore.protocol.rpc.client.RpcClient}
 */
@Deprecated
public interface RequestResponseClientProtocolStack extends
        TransportClientSource, CodecsSource, ServiceConfigurationSource, SchedulerSource {

    @Deprecated
    default MuonFuture<Response> request(String uri) {
        return request(uri, new Object());
    }
    @Deprecated
    default MuonFuture<Response> request(String uri, Object payload) {
        try {
            return request(new URI(uri), payload);
        } catch (URISyntaxException e) {
            throw new MuonException("URI Scheme is incorrect, must be scheme request:// for RPC requests");
        }
    }

    @Deprecated
    default MuonFuture<Response> request(URI uri, Object payload) {
        if (!uri.getScheme().equals(RRPTransformers.REQUEST_RESPONSE_PROTOCOL) && !uri.getScheme().equals("request")) {
            throw new MuonException("Scheme is invalid: " + uri.getScheme() + ", requires scheme: " + RRPTransformers.REQUEST_RESPONSE_PROTOCOL);
        }
        return request(new Request(uri, payload));
    }

    @Deprecated
    default MuonFuture<Response> request(Request event) {

        Channel<Request, Response> api2rrp = Channels.channel("rrpclientapi", "rrpclientproto");

        ChannelFutureAdapter<Response, Request> adapter =
                new ChannelFutureAdapter<>(api2rrp.left());


        Channel<MuonOutboundMessage, MuonInboundMessage> timeoutChannel = Channels.timeout(getScheduler(), 10000);

        ChannelConnection<MuonOutboundMessage, MuonInboundMessage> connection = getTransportClient().openClientChannel();
        Channels.connect(timeoutChannel.right(), connection);

        new RequestResponseClientProtocol(
                getConfiguration().getServiceName(),
                api2rrp.right(),
                timeoutChannel.left(),
                getCodecs(),
                getScheduler());

        return adapter.request(event);
    }
}
