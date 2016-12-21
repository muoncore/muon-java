package io.muoncore.example.guide;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.reactivestream.server.PublisherLookup;
import io.reactivex.Flowable;

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all;

public class Menu {

  public static void main(String[] args) {
    AutoConfiguration config =
      MuonConfigBuilder.withServiceIdentifier(  // <1>
        "menu").build();

    Muon muon = MuonBuilder.withConfig(config).build();  //<2>

    muon.handleRequest(all(), request -> {              //<3>
      request.ok("hello world");
    });

    muon.publishSource(                                 //<4>
      "mysource",
      PublisherLookup.PublisherType.HOT,
      Flowable.just("Hello", "World", "Item"));         //<5>
  }
}
