package io.muoncore.protocol.defaultproto;

import io.muoncore.Discovery;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.descriptors.SchemaDescriptor;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.transport.TransportEvents;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A default protocol intended to be the fallback if not other protocol is capable of processing an incoming message
 * <p>
 * Responds back with a 404 message.
 */
public class DefaultServerProtocol implements ServerProtocolStack {

  private Codecs codecs;
  private AutoConfiguration config;
  private Discovery discovery;

  public DefaultServerProtocol(Codecs codecs, AutoConfiguration config, Discovery discovery) {
    this.codecs = codecs;
    this.config = config;
    this.discovery = discovery;
  }

  @Override
  public ChannelConnection<MuonInboundMessage, MuonOutboundMessage> createChannel() {
    return new DefaultServerChannelConnection();
  }

  private class DefaultServerChannelConnection implements ChannelConnection<MuonInboundMessage, MuonOutboundMessage> {

    private ChannelFunction<MuonOutboundMessage> func;

    @Override
    public void receive(ChannelFunction<MuonOutboundMessage> function) {
      func = function;
    }

    @Override
    public void send(MuonInboundMessage message) {
      if (func != null && message != null) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("status", "404");
        metadata.put("message", "Protocol unknown :" + message.getProtocol());

        Codecs.EncodingResult result = codecs.encode(metadata, discovery.getCodecsForService(message.getSourceServiceName()));

        func.apply(MuonMessageBuilder
          .fromService(config.getServiceName())
          .step(TransportEvents.PROTOCOL_NOT_FOUND)
          .protocol(message.getProtocol())
          .toService(message.getSourceServiceName())
          .payload(result.getPayload())
          .contentType(result.getContentType())
          .status(MuonMessage.Status.error)
          .build()
        );
        shutdown();
      }
    }

    @Override
    public void shutdown() {
      func.apply(null);
    }
  }

  @Override
  public ProtocolDescriptor getProtocolDescriptor() {
    return new ProtocolDescriptor("default", "Default Protocol", "Returns 404 for all messages that match no other protocol", Collections.emptyList());
  }

  @Override
  public Map<String, SchemaDescriptor> getSchemasFor(String endpoint) {
    return Collections.emptyMap();
  }
}
