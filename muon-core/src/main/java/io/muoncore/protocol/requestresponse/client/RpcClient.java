package io.muoncore.protocol.requestresponse.client;

import io.muoncore.Muon;
import io.muoncore.api.ChannelFutureAdapter;
import io.muoncore.api.MuonFuture;
import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.exception.MuonException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.requestresponse.RRPTransformers;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import lombok.AllArgsConstructor;

import java.net.URI;
import java.net.URISyntaxException;

@AllArgsConstructor
public class RpcClient {
  private Muon muon;

  public MuonFuture<Response> request(String uri) {
    return request(uri, new Object());
  }
  public MuonFuture<Response> request(String uri, Object payload) {
    try {
      return request(new URI(uri), payload);
    } catch (URISyntaxException e) {
      throw new MuonException("URI Scheme is incorrect, must be scheme request:// for RPC requests");
    }
  }

  public MuonFuture<Response> request(URI uri, Object payload) {
    if (!uri.getScheme().equals(RRPTransformers.REQUEST_RESPONSE_PROTOCOL) && !uri.getScheme().equals("request")) {
      throw new MuonException("Scheme is invalid: " + uri.getScheme() + ", requires scheme: " + RRPTransformers.REQUEST_RESPONSE_PROTOCOL);
    }
    return request(new Request(uri, payload));
  }

  public MuonFuture<Response> request(Request event) {

    Channel<Request, Response> api2rrp = Channels.channel("rrpclientapi", "rrpclientproto");

    ChannelFutureAdapter<Response, Request> adapter =
      new ChannelFutureAdapter<>(api2rrp.left());


    Channel<MuonOutboundMessage, MuonInboundMessage> timeoutChannel = Channels.timeout(muon.getScheduler(), 10000);

    ChannelConnection<MuonOutboundMessage, MuonInboundMessage> connection = muon.getTransportClient().openClientChannel();
    Channels.connect(timeoutChannel.right(), connection);

    new RequestResponseClientProtocol(
      muon.getConfiguration().getServiceName(),
      api2rrp.right(),
      timeoutChannel.left(),
      muon.getCodecs(),
      muon.getScheduler());

    return adapter.request(event);
  }
}
