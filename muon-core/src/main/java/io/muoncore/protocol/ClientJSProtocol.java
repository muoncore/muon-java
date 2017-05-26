package io.muoncore.protocol;

import io.muoncore.Muon;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.exception.MuonException;
import io.muoncore.transport.client.TransportClient;

public class ClientJSProtocol extends JSProtocol {
  public ClientJSProtocol(Muon muon, String protocolName, ChannelConnection leftChannelConnection) throws MuonException {
    super(muon, protocolName);

    setApiChannel(leftChannelConnection);

    TransportClient transportClient = muon.getTransportClient();
    setTransportChannel(transportClient.openClientChannel());
  }
}

