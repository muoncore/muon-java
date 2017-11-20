package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.rpc.server.RpcServer;

import java.util.Collections;
import java.util.Map;

import static io.muoncore.protocol.rpc.server.HandlerPredicates.path;

// tag::main[]
public class MuonServerExample {

  public static void main(String[] args) {

    AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("my-test-service")               // <1>
      .addWriter(conf -> {
        conf.getProperties().put("muon.discovery.factories", "io.muoncore.discovery.amqp.AmqpDiscoveryFactory"); // <2>
        conf.getProperties().put("amqp.discovery.url", "amqp://muon:microservices@localhost");

        conf.getProperties().put("muon.transport.factories", "io.muoncore.transport.amqp.AmqpMuonTransportFactory");  // <3>
        conf.getProperties().put("amqp.transport.url", "amqp://muon:microservices@localhost");

      })
      .build();

    Muon muon = MuonBuilder.withConfig(config).build();              // <4>

    RpcServer rpc = new RpcServer(muon);                             // <5>

    rpc.handleRequest(path("/"))                                     // <6>
      .addResponseType(Map.class)
      .handler(request -> {
        request.ok(Collections.singletonMap("message", "hello world"));  // <7>
      });
  }
}
// end::main[]
