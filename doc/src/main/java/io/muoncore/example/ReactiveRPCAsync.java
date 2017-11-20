package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.protocol.rpc.Response;
import io.muoncore.protocol.rpc.client.RpcClient;
import io.muoncore.protocol.rpc.server.HandlerPredicates;
import io.muoncore.protocol.rpc.server.RequestWrapper;
import io.muoncore.protocol.rpc.server.RpcServer;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import static io.muoncore.protocol.rpc.server.HandlerPredicates.all;

public class ReactiveRPCAsync {

  public void exec(Muon muon) throws ExecutionException, InterruptedException {

    // tag::main[]
    RpcServer rpcServer = new RpcServer(muon);

    Queue<RequestWrapper> requestQueue = new LinkedList<>();

    rpcServer.handleRequest(all(), requestQueue::add);    // <1>

    new Thread(() -> {   // <2>

      while (true) {

        RequestWrapper wrapper = requestQueue.poll();

        wrapper.ok("Hello");       // <3>

        if (requestQueue.isEmpty()) {
          // ... wait ....
        }
      }
      // <4>
    });

    // end::main[]
  }
}
