package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.rpc.client.RpcClient;
import io.muoncore.protocol.rpc.server.RpcServer;

import java.util.Collections;
import java.util.Map;

import static io.muoncore.protocol.rpc.server.HandlerPredicates.path;

// tag::main[]
public class MuonClientExample {

  public static void main(String[] args) {

    // tag::config[]
    AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("my-test-service")               // <1>
      .addWriter(conf -> {
        conf.getProperties().put("muon.discovery.factories", "io.muoncore.discovery.amqp.AmqpDiscoveryFactory"); // <2>
        conf.getProperties().put("amqp.discovery.url", "amqp://muon:microservices@localhost");

        conf.getProperties().put("muon.transport.factories", "io.muoncore.transport.amqp.AmqpMuonTransportFactory");  // <3>
        conf.getProperties().put("amqp.transport.url", "amqp://muon:microservices@localhost");

      })
      .build();
    // end::config[]

    Muon muon = MuonBuilder.withConfig(config).build();              // <4>

    RpcClient rpc = new RpcClient(muon);                             // <5>

    rpc.request("rpc://example-service/").then(arg -> {
      System.out.println("Got " + arg.getStatus());
      System.out.println("Got " + arg.getPayload(Map.class));
    });
  }
}
// end::main[]
