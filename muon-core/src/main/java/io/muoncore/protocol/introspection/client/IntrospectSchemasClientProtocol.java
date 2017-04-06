package io.muoncore.protocol.introspection.client;

import io.muoncore.Discovery;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.descriptors.SchemasDescriptor;
import io.muoncore.descriptors.ServiceExtendedDescriptor;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.introspection.SchemaIntrospectionRequest;

import static io.muoncore.protocol.introspection.server.IntrospectionServerProtocolStack.PROTOCOL;

public class IntrospectSchemasClientProtocol<X, R> {

  public static final String SCHEMA_INTROSPECTION_REQUESTED = "schemaIntrospection";

  private Codecs codecs;

  public IntrospectSchemasClientProtocol(
    String serviceName,
    AutoConfiguration config,
    final ChannelConnection<SchemasDescriptor, SchemaIntrospectionRequest> leftChannelConnection,
    final ChannelConnection<MuonOutboundMessage, MuonInboundMessage> rightChannelConnection,
    final Codecs codecs,
    final Discovery discovery) {

    rightChannelConnection.receive(message -> {
      if (message == null) {
        leftChannelConnection.shutdown();
        return;
      }
      SchemasDescriptor descript = codecs.decode(message.getPayload(), message.getContentType(), SchemasDescriptor.class);
      leftChannelConnection.send(descript);
    });

    leftChannelConnection.receive(request -> {
      if (request == null) {
        rightChannelConnection.shutdown();
        return;
      }

      Codecs.EncodingResult result = codecs.encode(request,
        discovery.getCodecsForService(serviceName));

      MuonOutboundMessage msg = MuonMessageBuilder
        .fromService(config.getServiceName())
        .step(SCHEMA_INTROSPECTION_REQUESTED)
        .protocol(PROTOCOL)
        .toService(serviceName)
        .payload(result.getPayload())
        .contentType(result.getContentType())
        .status(MuonMessage.Status.success)
        .build();

      rightChannelConnection.send(msg);
    });

    /**
     * handle 404.
     * handle local timeout.
     *
     *
     */
  }
}
