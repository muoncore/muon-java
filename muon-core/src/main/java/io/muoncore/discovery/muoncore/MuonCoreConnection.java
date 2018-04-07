package io.muoncore.discovery.muoncore;


import io.muoncore.codec.Codecs;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.codec.types.MuonCodecTypes;
import io.muoncore.transport.saas.MuonCoreTransport;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@WebSocket(maxTextMessageSize = 64 * 1024)
public class MuonCoreConnection {

  private static MuonCoreConnection INSTANCE;

  public static synchronized MuonCoreConnection connection() {
    if (INSTANCE == null)  {
      INSTANCE = new MuonCoreConnection();
    }
    return INSTANCE;
  }

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
    client = new WebSocketClient();
    try {
      client.start();

      URI echoUri = new URI("ws://localhost:8080");
      ClientUpgradeRequest request = new ClientUpgradeRequest();
      session = client.connect(this, echoUri).get();
      System.out.printf("Connected to : %s%n",echoUri);
    } catch (Throwable t) {
      log.error("Error connecting to remote", t);
      reconnect();
    }
  }

  public void reconnect() {
    this.session = null;

    if (isShutdown) return;

    try {
      Thread.sleep(5000);
      start();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @OnWebSocketClose
  public void onClose(int statusCode, String reason)
  {
    log.info("Connection closed: %d - %s%n",statusCode,reason);
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
    System.out.printf("Got connect: %s%n",session);
    this.session = session;
  }

  @OnWebSocketMessage
  public void onMessage(String msg) {
    System.out.printf("Got msg: %s%n",msg);

    MuonCoreMessage message = codecs.decode(msg.getBytes(), "application/json", MuonCoreMessage.class);

    System.out.printf("MSG TYPE: %s%n", message.getType());

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
}
