package io.muoncore.protocol;

import io.muoncore.Muon;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.exception.MuonException;

public class ServerJSProtocol extends JSProtocol {
  public ServerJSProtocol(Muon muon, String protocolName, ChannelConnection transportconnection) throws MuonException {
    super(muon, protocolName);
    setTransportChannel(transportconnection);
  }
}
