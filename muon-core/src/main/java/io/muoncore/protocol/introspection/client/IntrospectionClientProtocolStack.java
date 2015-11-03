package io.muoncore.protocol.introspection.client;

import io.muoncore.channel.ChannelFutureAdapter;
import io.muoncore.channel.async.StandardAsyncChannel;
import io.muoncore.codec.CodecsSource;
import io.muoncore.config.MuonConfigurationSource;
import io.muoncore.descriptors.ServiceExtendedDescriptor;
import io.muoncore.future.MuonFuture;
import io.muoncore.transport.TransportClientSource;

public interface IntrospectionClientProtocolStack extends TransportClientSource, CodecsSource, MuonConfigurationSource {
    default MuonFuture<ServiceExtendedDescriptor> introspect(String serviceName) {

        StandardAsyncChannel<String, ServiceExtendedDescriptor> api2rrp = new StandardAsyncChannel<>();

        ChannelFutureAdapter<ServiceExtendedDescriptor, String> adapter =
                new ChannelFutureAdapter<>(api2rrp.left());

        new IntrospectClientProtocol<>(
                serviceName,
                getConfiguration(),
                api2rrp.right(),
                getTransportClient().openClientChannel(),
                getCodecs());

        return adapter.request(serviceName);
    }
}
