package io.muoncore.examples;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.reactivestream.server.PublisherLookup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.rx.broadcast.Broadcaster;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.path;

public class ServiceAuth {

  public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {

    AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("stream-test").build();

    Muon muon = MuonBuilder.withConfig(config).build();

    muon.getDiscovery().blockUntilReady();

    muon.handleRequest(path("/"), request -> {
      request.ok("Hello World");
    });

    muon.handleRequest(path("/in"))
      .addRequestType(MyRequest.class)
      .addResponseType(MyResponse.class)
      .handler(request -> {
        MyRequest myReq = request.getRequest().getPayload(MyRequest.class);
        //do something with the request
        request.ok(new MyResponse("Hello World"));
      })
      .build();
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  static class MyRequest {
    String name;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  static class MyResponse {
    String name;
  }
}