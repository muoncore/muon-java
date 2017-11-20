package io.muoncore.example;

import io.muoncore.MultiTransportMuon;
import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.codec.json.GsonCodec;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.discovery.MultiDiscovery;
import io.muoncore.memory.discovery.InMemDiscovery;
import io.muoncore.memory.transport.InMemTransport;
import io.muoncore.memory.transport.bus.EventBus;
import io.muoncore.protocol.rpc.client.RpcClient;

import java.util.Collections;
import java.util.Map;

public class ManualMuonBuild {

  public static void main(String[] args) {

    // tag::transport[]
    EventBus bus = new EventBus();

    AutoConfiguration config = MuonConfigBuilder
           .withServiceIdentifier("my-test-service").build();   // <1>

    Muon muon = new MultiTransportMuon(                         // <2>
      config,
      new InMemDiscovery(),
      Collections.singletonList(new InMemTransport(config, bus)),  // <3>
      new JsonOnlyCodecs());
    // end::discovery[]

    // tag::discovery[]
    Muon muondisco = new MultiTransportMuon(                         // <1>
      config,
      new MultiDiscovery(Collections.singletonList(new InMemDiscovery())), // <2>
      Collections.singletonList(new InMemTransport(config, bus)),
      new JsonOnlyCodecs());
    // end::discovery[]
  }
}
