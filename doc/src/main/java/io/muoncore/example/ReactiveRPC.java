package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.protocol.requestresponse.Response;

import java.util.concurrent.ExecutionException;

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all;

public class ReactiveRPC {

    public void exec(Muon muon) throws ExecutionException, InterruptedException {

        //request handler
        muon.handleRequest(all(), request -> {
            request.ok("Hi There");
        });

        //request client
        Response data = muon.request("request://myservice/").get();

        System.out.println("The Data is " + data.getPayload(String.class));
    }
}
