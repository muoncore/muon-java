package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.protocol.rpc.Response;
import io.muoncore.protocol.rpc.client.RpcClient;
import io.muoncore.protocol.rpc.server.HandlerPredicates;
import io.muoncore.protocol.rpc.server.RpcServer;

import java.util.concurrent.ExecutionException;


public class ReactiveRPC {

  // tag::server[]
  public void server(Muon muon) throws ExecutionException, InterruptedException {
    RpcServer rpcServer = new RpcServer(muon);

    //request handler
    rpcServer.handleRequest(HandlerPredicates.all(), request -> {
      request.ok("Hi There");
    });

  }
  // end::server[]

  // tag::client[]
  public void client(Muon muon) throws ExecutionException, InterruptedException {
    RpcClient rpcClient = new RpcClient(muon);

    Response data = rpcClient.request("request://myservice/").get();
    System.out.println("The Data is " + data.getPayload(String.class));

  }
  // end::client[]
}
