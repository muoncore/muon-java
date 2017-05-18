package io.muoncore.protocol.reactivestream.server;

import io.muoncore.Discovery;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.descriptors.OperationDescriptor;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.descriptors.SchemaDescriptor;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReactiveStreamServerStack implements ServerProtocolStack {

  public static String REACTIVE_STREAM_PROTOCOL = "reactive-stream";

  private PublisherLookup publisherLookup;
  private Codecs codecs;
  private AutoConfiguration configuration;
  private Discovery discovery;

  public ReactiveStreamServerStack(
    PublisherLookup publisherLookup,
    Codecs codecs,
    AutoConfiguration configuration, Discovery discovery) {
    this.publisherLookup = publisherLookup;
    this.codecs = codecs;
    this.configuration = configuration;
    this.discovery = discovery;
  }

  @Override
  public ChannelConnection<MuonInboundMessage, MuonOutboundMessage> createChannel() {
    return new ReactiveStreamServerChannel(publisherLookup, codecs, configuration, discovery);
  }

  @Override
  public ProtocolDescriptor getProtocolDescriptor() {

    List<OperationDescriptor> ops = publisherLookup.getPublishers().stream().map(
      pub -> new OperationDescriptor(pub.getName(), "[" + pub.getPublisherType() + "]")
    ).collect(Collectors.toList());

    return new ProtocolDescriptor(REACTIVE_STREAM_PROTOCOL, "Reactive Streaming", "Provides the semantics of the Reactive Stream API over a muon event protocol",
      ops);
  }

  @Override
  public Map<String, SchemaDescriptor> getSchemasFor(String endpoint) {




    return null;
  }
}
