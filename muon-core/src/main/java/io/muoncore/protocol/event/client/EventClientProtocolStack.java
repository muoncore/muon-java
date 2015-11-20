package io.muoncore.protocol.event.client;

import io.muoncore.DiscoverySource;
import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelFutureAdapter;
import io.muoncore.channel.Channels;
import io.muoncore.codec.CodecsSource;
import io.muoncore.config.MuonConfigurationSource;
import io.muoncore.future.MuonFuture;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocol;
import io.muoncore.transport.TransportClientSource;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.Map;

public interface EventClientProtocolStack extends
        TransportClientSource, DiscoverySource, CodecsSource, MuonConfigurationSource {

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
