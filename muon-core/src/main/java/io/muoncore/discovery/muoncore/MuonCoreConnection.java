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

  private String wsUrl;
  @Setter
  private MuonCoreDiscovery discovery;
  @Setter
  private MuonCoreTransport transport;

  private WebSocketClient client;
  private Session session;
  private Codecs codecs = new JsonOnlyCodecs();
  private boolean disconnectionLogged = false;

  public MuonCoreConnection(String wsUrl) {
    this.wsUrl = wsUrl;
  }

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
      URI echoUri = new URI(this.wsUrl);
      ClientUpgradeRequest request = new ClientUpgradeRequest();
      session = client.connect(this, echoUri).get();
      discovery.advertiseCurrentService();
      log.info("Client has fully connected and advertised the current service descriptor, ready for service");
      disconnectionLogged = false;
    } catch (Throwable t) {
      if (!disconnectionLogged) {
        log.error("Error connecting to remote, will start reconnection cycle and report on successful reconnection", t);
        disconnectionLogged = true;
      }
      reconnect();
    }
  }

  public void reconnect() {
    this.client = null;
    this.session = null;

    if (isShutdown) return;

    try {
      Thread.sleep(500);
      start();
    } catch (InterruptedException e) {

    }
  }

  @OnWebSocketClose
  public void onClose(int statusCode, String reason)
  {
    log.warn("Disconnected {}/ {}... will reconnect now", statusCode, reason);
    reconnect();
  }

  @OnWebSocketError
  public void error(Throwable throwable) {
//    log.warn("Error in connection", throwable);
//    reconnect();
  }

  @OnWebSocketConnect
  public void onConnect(Session session)
  {
    this.session = session;
  }

  @OnWebSocketMessage
  public void onMessage(String msg) {

    MuonCoreMessage message = codecs.decode(msg.getBytes(), "application/json", MuonCoreMessage.class);

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

      String url = (String) config.getProperties().get("muoncore.url");

      if (url == null) {
        url = "wss://ws.muoncore.io";
        log.info("muoncore.url is not defined, defaulting to " + url);
      }

      connection = new MuonCoreConnection(url);
      config.getProperties().put("muoncore.connection", connection);
    }
    return connection;
  }
}
