package io.muoncore.transport.saas;

import io.muoncore.Discovery;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.support.Scheduler;
import io.muoncore.codec.Codecs;
import io.muoncore.discovery.muoncore.MuonCoreConnection;
import io.muoncore.discovery.muoncore.MuonCoreMessage;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.exception.NoSuchServiceException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.transport.MuonTransport;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
public class MuonCoreTransport implements MuonTransport {

  private final String ID;
  private ServerStacks stacks;
  private Codecs codecs;
  private MuonCoreConnection connection;
  private final Map<String, Consumer<MuonInboundMessage>> inboundHandlers = new HashMap<>();

  public MuonCoreTransport(MuonCoreConnection connection) {
    this.connection = connection;
    this.ID = connection.ID;
  }

  @Override
  public void shutdown() {
    connection.shutdown();
  }

  @Override
  public void start(Discovery discovery, ServerStacks serverStacks, Codecs codecs, Scheduler scheduler) throws MuonTransportFailureException {
    this.stacks = serverStacks;
    this.codecs = codecs;
    connection.setTransport(this);
    connection.start();
  }

  @Override
  public String getUrlScheme() {
    return "muoncore";
  }

  @Override
  public URI getLocalConnectionURI() {
    try {
      // TODO, get the url from the configured endpoint ...
      return new URI("muoncore://cloud.muoncore.io");
    } catch (URISyntaxException e) {
      throw new MuonTransportFailureException("Incorrect URI for muon core connection", e);
    }
  }

  @Override
  public boolean canConnectToService(String name) {
    //TODO, really check ..
    return true;
  }

  @Override
  public ChannelConnection<MuonOutboundMessage, MuonInboundMessage> openClientChannel(String serviceName, String protocol) throws NoSuchServiceException, MuonTransportFailureException {
    MuonCoreChannelConnection muonCoreChannelConnection = new MuonCoreChannelConnection(serviceName, protocol, codecs, connection);
    inboundHandlers.put(muonCoreChannelConnection.getChannelId(), muonCoreChannelConnection::sendInternal);
    return muonCoreChannelConnection;
  }

  public void handle(MuonCoreMessage message) {

    log.info(new String(message.getData()));

    MuonInboundMessage decode = codecs.decode(message.getData(), "application/json", MuonInboundMessage.class);

    if (message.getStep().equals("dat")) {
      Consumer<MuonInboundMessage> consumer = inboundHandlers.get(message.getCorrelationId());

      if (consumer == null) {
        ChannelConnection<MuonInboundMessage, MuonOutboundMessage> chan = this.stacks.openServerChannel(decode.getProtocol());

        chan.receive(arg -> {
          try {
            if (arg == null) {
              return;
            }
            connection.send(new MuonCoreMessage("transport", "dat",message.getCorrelationId(),
              codecs.encode(arg, codecs.getAvailableCodecs()).getPayload()));
          } catch (IOException e) {
            inboundHandlers.remove(message.getCorrelationId());
          }
        });

        inboundHandlers.put(message.getCorrelationId(), muonInboundMessage -> {
          if (muonInboundMessage.getChannelOperation() == MuonMessage.ChannelOperation.closed) {
            chan.shutdown();
          } else {
            chan.send(muonInboundMessage);
          }
        });
        consumer = chan::send;
      }

      consumer.accept(decode);

    } else if (message.getStep().equals("shutdown")) {
      inboundHandlers.get(message.getCorrelationId()).accept(decode);
      inboundHandlers.remove(message.getCorrelationId());
    }
  }
}
