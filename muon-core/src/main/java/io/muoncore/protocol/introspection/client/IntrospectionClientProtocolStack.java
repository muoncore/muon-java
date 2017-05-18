package io.muoncore.protocol.introspection.client;

import io.muoncore.DiscoverySource;
import io.muoncore.channel.Channel;
import io.muoncore.api.ChannelFutureAdapter;
import io.muoncore.channel.Channels;
import io.muoncore.codec.CodecsSource;
import io.muoncore.config.MuonConfigurationSource;
import io.muoncore.descriptors.SchemasDescriptor;
import io.muoncore.descriptors.ServiceExtendedDescriptor;
import io.muoncore.api.MuonFuture;
import io.muoncore.protocol.introspection.SchemaIntrospectionRequest;
import io.muoncore.transport.TransportClientSource;

public interface IntrospectionClientProtocolStack extends TransportClientSource, CodecsSource, MuonConfigurationSource, DiscoverySource {
  default MuonFuture<ServiceExtendedDescriptor> introspect(String serviceName) {

    Channel<String, ServiceExtendedDescriptor> api2rrp = Channels.channel("introspectionapi", "introspectionproto");

    ChannelFutureAdapter<ServiceExtendedDescriptor, String> adapter =
      new ChannelFutureAdapter<>(api2rrp.left());

    new IntrospectClientProtocol<>(
      serviceName,
      getConfiguration(),
      api2rrp.right(),
      getTransportClient().openClientChannel(),
      getCodecs(), getDiscovery());

    return adapter.request(serviceName);
  }

  default MuonFuture<SchemasDescriptor> getSchemas(String serviceName, String protocol, String endpoint) {
    Channel<SchemaIntrospectionRequest, SchemasDescriptor> api2rrp = Channels.channel("introspectionapi", "introspectionproto");

    ChannelFutureAdapter<SchemasDescriptor, SchemaIntrospectionRequest> adapter =
      new ChannelFutureAdapter<>(api2rrp.left());

    new IntrospectSchemasClientProtocol<>(
      serviceName,
      getConfiguration(),
      api2rrp.right(),
      getTransportClient().openClientChannel(),
      getCodecs(), getDiscovery());

    return adapter.request(new SchemaIntrospectionRequest(protocol, endpoint));
  }
}
