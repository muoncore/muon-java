package io.muoncore.protocol.rpc.client;

import io.muoncore.Muon;
import io.muoncore.api.ChannelFutureAdapter;
import io.muoncore.api.MuonFuture;
import io.muoncore.channel.Channel;
import io.muoncore.channel.Channels;
import io.muoncore.exception.MuonException;
import io.muoncore.protocol.ClientJSProtocol;
import io.muoncore.protocol.JSProtocol;
import io.muoncore.protocol.rpc.Request;
import io.muoncore.protocol.rpc.Response;
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
    if (!uri.getScheme().equals("rpc") && !uri.getScheme().equals("request")) {
      throw new MuonException("Scheme is invalid: " + uri.getScheme() + ", requires scheme: rpc://");
    }
    return request(new Request(uri, payload));
  }

  public MuonFuture<Response> request(Request event) {

    Channel<Request, Response> api2rrp = Channels.channel("rrpclientapi", "rrpclientproto");

    ChannelFutureAdapter<Response, Request> adapter =
      new ChannelFutureAdapter<>(api2rrp.left());

    JSProtocol proto = new ClientJSProtocol(muon, "rpc", api2rrp.right());
    proto.addTypeForCoercion("Response", map -> {
      return new Response(
        (Integer) map.get("status"),
        null, null, muon.getCodecs()
      );
    });
    proto.addPostDecodingDecorator(Response.class, response -> {
      response.setCodecs(muon.getCodecs());
      return response;
    });
    proto.addTypeForDecoding("Response", Response.class);
    proto.start(RpcClient.class.getResourceAsStream("/rpc-client.js"));

    return adapter.request(event);
  }
}
