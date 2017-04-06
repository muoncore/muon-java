package io.muoncore.protocol.introspection.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.descriptors.SchemasDescriptor;
import io.muoncore.descriptors.ServiceExtendedDescriptor;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.introspection.SchemaIntrospectionRequest;
import io.muoncore.protocol.introspection.client.IntrospectClientProtocol;

import static io.muoncore.protocol.introspection.client.IntrospectSchemasClientProtocol.SCHEMA_INTROSPECTION_REQUESTED;

class IntrospectionServerChannelConnection implements ChannelConnection<MuonInboundMessage, MuonOutboundMessage> {

  private IntrospectionServerProtocolStack introspectionServerProtocolStack;
  private ChannelFunction<MuonOutboundMessage> func;

  public IntrospectionServerChannelConnection(IntrospectionServerProtocolStack introspectionServerProtocolStack) {
    this.introspectionServerProtocolStack = introspectionServerProtocolStack;
  }

  @Override
  public void receive(ChannelFunction<MuonOutboundMessage> function) {
    func = function;
  }

  @Override
  public void send(MuonInboundMessage message) {
    if (message == null) {
      if (func != null) func.apply(null);
      return;
    }

    Codecs.EncodingResult result;

    if (message.getStep().equals(SCHEMA_INTROSPECTION_REQUESTED)) {
      SchemaIntrospectionRequest introspection = introspectionServerProtocolStack.getCodecs().decode(message.getPayload(), message.getContentType(), SchemaIntrospectionRequest.class);
      SchemasDescriptor descriptor = introspectionServerProtocolStack.getDescriptorSource().getSchemasDescriptor(introspection);


      result = introspectionServerProtocolStack.getCodecs().encode(descriptor,
        introspectionServerProtocolStack.getDiscovery().getCodecsForService(message.getSourceServiceName()));
    } else {
      ServiceExtendedDescriptor descriptor = introspectionServerProtocolStack.getDescriptorSource().getServiceExtendedDescriptor();
      result = introspectionServerProtocolStack.getCodecs().encode(descriptor,
        introspectionServerProtocolStack.getDiscovery().getCodecsForService(message.getSourceServiceName()));
    }

    func.apply(MuonMessageBuilder
        .fromService(message.getTargetServiceName())
        .step("introspectionReport")
        .protocol(IntrospectionServerProtocolStack.PROTOCOL)
        .toService(message.getSourceServiceName())
        .payload(result.getPayload())
        .contentType(result.getContentType())
        .status(MuonMessage.Status.success)
//                    .operation(MuonMessage.ChannelOperation.closed)
        .build()
    );
    shutdown();
  }

  @Override
  public void shutdown() {
    if (func != null) {
      func.apply(null);
      func = null;
    }
  }
}
