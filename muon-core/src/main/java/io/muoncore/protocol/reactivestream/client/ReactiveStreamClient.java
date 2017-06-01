package io.muoncore.protocol.reactivestream.client;

import io.muoncore.Muon;
import io.muoncore.exception.MuonException;
import lombok.AllArgsConstructor;
import org.reactivestreams.Subscriber;

import java.net.URI;

@AllArgsConstructor
public class ReactiveStreamClient {
  private Muon muon;

  public void subscribe(URI uri, Subscriber<StreamData> subscriber) {
    if (!uri.getScheme().equals("stream")) throw new IllegalArgumentException("URI Scheme is invalid. Requires scheme: stream://");

    if (muon.getDiscovery().getServiceNamed(uri.getHost()).isPresent()) {

      ReactiveStreamClientProtocol proto = new ReactiveStreamClientProtocol(
        uri,
        muon.getTransportClient().openClientChannel(),
        subscriber,
        muon.getCodecs(),
        muon.getConfiguration(), muon.getDiscovery());

      proto.start();
    } else {
      throw new MuonException("The service " + uri.getHost() + " is not currently available");
    }
  }
}
