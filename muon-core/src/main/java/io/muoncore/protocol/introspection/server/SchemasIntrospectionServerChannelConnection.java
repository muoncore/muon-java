package io.muoncore.protocol.introspection.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.descriptors.ServiceExtendedDescriptor;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;

class SchemasIntrospectionServerChannelConnection implements ChannelConnection<MuonInboundMessage, MuonOutboundMessage> {

  private IntrospectionServerProtocolStack introspectionServerProtocolStack;
  private ChannelFunction<MuonOutboundMessage> func;

  public SchemasIntrospectionServerChannelConnection(IntrospectionServerProtocolStack introspectionServerProtocolStack) {
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

    ServiceExtendedDescriptor descriptor = introspectionServerProtocolStack.getDescriptorSource().getServiceExtendedDescriptor();

    Codecs.EncodingResult result = introspectionServerProtocolStack.getCodecs().encode(descriptor,
      introspectionServerProtocolStack.getDiscovery().getCodecsForService(message.getSourceServiceName()));

    func.apply(MuonMessageBuilder
        .fromService(descriptor.getServiceName())
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
