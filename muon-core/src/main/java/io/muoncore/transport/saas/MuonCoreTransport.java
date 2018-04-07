package io.muoncore.transport.saas;

import io.muoncore.Discovery;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.support.Scheduler;
import io.muoncore.codec.Codecs;
import io.muoncore.discovery.muoncore.MuonCoreMessage;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.exception.NoSuchServiceException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.transport.MuonTransport;

import java.net.URI;

public class MuonCoreTransport implements MuonTransport {

  @Override
  public void shutdown() {

  }

  @Override
  public void start(Discovery discovery, ServerStacks serverStacks, Codecs codecs, Scheduler scheduler) throws MuonTransportFailureException {
//    WebSocketClient client = new WebSocketClient();
//    MuonCoreWSSocket socket = new MuonCoreWSSocket();
//
//    try {
//      client.start();
//      URI echoUri = new URI("ws://localhost:8080");
//      ClientUpgradeRequest request = new ClientUpgradeRequest();
//      client.connect(socket,echoUri,request);
//      System.out.printf("Connecting to : %s%n",echoUri);
//
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
  }

  @Override
  public String getUrlScheme() {
    return null;
  }

  @Override
  public URI getLocalConnectionURI() {
    return null;
  }

  @Override
  public boolean canConnectToService(String name) {
    return false;
  }

  @Override
  public ChannelConnection<MuonOutboundMessage, MuonInboundMessage> openClientChannel(String serviceName, String protocol) throws NoSuchServiceException, MuonTransportFailureException {
    return null;
  }

  public void handle(MuonCoreMessage message) {
    System.out.println("Got a message from remote ... " + message);
  }
}
