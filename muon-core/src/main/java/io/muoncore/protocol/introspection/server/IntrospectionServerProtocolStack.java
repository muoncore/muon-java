package io.muoncore.protocol.introspection.server;

import io.muoncore.Discovery;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.descriptors.SchemaDescriptor;
import io.muoncore.descriptors.ServiceExtendedDescriptorSource;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerProtocolStack;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
public class IntrospectionServerProtocolStack implements ServerProtocolStack {

  private ServiceExtendedDescriptorSource descriptorSource;
  private Codecs codecs;
  private Discovery discovery;
  public final static String PROTOCOL = "introspect";

  public IntrospectionServerProtocolStack(ServiceExtendedDescriptorSource descriptorSource, Codecs codecs, Discovery discovery) {
    this.descriptorSource = descriptorSource;
    this.codecs = codecs;
    this.discovery = discovery;
  }

  @Override
  public ChannelConnection<MuonInboundMessage, MuonOutboundMessage> createChannel() {
    return new IntrospectionServerChannelConnection(this);
  }

  @Override
  public ProtocolDescriptor getProtocolDescriptor() {
    return new ProtocolDescriptor(
      PROTOCOL,
      "Introspection Protocol",
      "Provides the ability to introspect services to derive their capabilities. Built into most Muon implementations",
      Collections.emptyList());
  }

  @Override
  public Map<String, SchemaDescriptor> getSchemasFor(String endpoint) {
    return Collections.emptyMap();
  }
}
