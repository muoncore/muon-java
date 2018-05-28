package io.muoncore.discovery.muoncore;


import io.muoncore.codec.Codecs;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.transport.saas.MuonCoreTransport;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Slf4j
@WebSocket(maxTextMessageSize = 64 * 1024)
public class MuonCoreConnection {

  public final String ID = UUID.randomUUID().toString();
  private boolean isShutdown = false;

  @Setter
  private MuonCoreDiscovery discovery;
  @Setter
  private MuonCoreTransport transport;

  private WebSocketClient client;
  private Session session;
  private Codecs codecs = new JsonOnlyCodecs();

  public void send(MuonCoreMessage message) throws IOException {
    byte[] payload = codecs.encode(message, codecs.getAvailableCodecs()).getPayload();
    String val = new String(payload);
    session.getRemote().sendString(val);
  }

  public void shutdown() {
    isShutdown = true;
    try {
      client.stop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void start() {
    if (client != null) return;

    client = new WebSocketClient();
    try {
      client.start();

      URI echoUri = new URI("ws://localhost:8080");
      ClientUpgradeRequest request = new ClientUpgradeRequest();
      session = client.connect(this, echoUri).get();
    } catch (Throwable t) {
      log.error("Error connecting to remote", t);
      reconnect();
    }
  }

  public void reconnect() {
    this.session = null;

    if (isShutdown) return;

    try {
      Thread.sleep(500);
      start();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @OnWebSocketClose
  public void onClose(int statusCode, String reason)
  {
    reconnect();
  }

  @OnWebSocketError
  public void error(Throwable throwable) {
    log.warn("Error in connection", throwable);
    reconnect();
  }

  @OnWebSocketConnect
  public void onConnect(Session session)
  {
    this.session = session;
  }

  @OnWebSocketMessage
  public void onMessage(String msg) {

    MuonCoreMessage message = codecs.decode(msg.getBytes(), "application/json", MuonCoreMessage.class);

    log.info("MESSAGE " + message);

    if (message.getType().equals("discovery")) {
      if (discovery != null) {
        discovery.handle(message);
      }
    } else {
      if (transport != null) {
        transport.handle(message);
      }
    }
  }

  public static MuonCoreConnection extractFromAutoConfig(AutoConfiguration config) {
    MuonCoreConnection connection = (MuonCoreConnection) config.getProperties().get("muoncore.connection");
    if (connection == null) {
      connection = new MuonCoreConnection();
      config.getProperties().put("muoncore.connection", connection);
    }
    return connection;
  }
}
