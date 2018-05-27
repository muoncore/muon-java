package io.muoncore.transport.saas;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.discovery.muoncore.MuonCoreConnection;
import io.muoncore.discovery.muoncore.MuonCoreMessage;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class MuonCoreChannelConnection implements ChannelConnection<MuonOutboundMessage, MuonInboundMessage> {

  private String serviceName;
  private String protocol;
  private Codecs codecs;
  @Getter
  private final String channelId = UUID.randomUUID().toString();

  private ChannelFunction<MuonInboundMessage> function;
  private MuonCoreConnection connection;

  public MuonCoreChannelConnection(String serviceName, String protocol, Codecs codecs, MuonCoreConnection connection) {
    this.serviceName = serviceName;
    this.protocol = protocol;
    this.codecs = codecs;
    this.connection = connection;
  }

  @Override
  public void receive(ChannelFunction<MuonInboundMessage> function) {
    this.function = function;
  }

  @Override
  public void send(MuonOutboundMessage message) {
    try {
      if (message == null) {
        return;
      }
      connection.send(new MuonCoreMessage(
        "transport", "dat", channelId, codecs.encode(message, codecs.getAvailableCodecs()).getPayload()
      ));
    } catch (IOException e) {
      log.warn("Cannot send message to remote", e);
    }
  }

  public void sendInternal(MuonInboundMessage inboundMessage) {
    this.function.apply(inboundMessage);
  }

  @Override
  public void shutdown() {
    log.info("Channel has been shutdown");
  }
}
