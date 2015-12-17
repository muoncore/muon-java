package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.protocol.requestresponse.server.RequestWrapper;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all;

public class ReactiveRPCAsync {

    public void exec(Muon muon) throws ExecutionException, InterruptedException {

        Queue<RequestWrapper> requestQueue = new LinkedList<>();

        //request handler
        muon.handleRequest(all(), Object.class, requestQueue::add);

        new Thread(() -> {

            while(true) {

                RequestWrapper wrapper = requestQueue.poll();

                wrapper.ok("Hello");

                if (requestQueue.isEmpty()) {
                    // ... wait ....
                }
            }
        });

        //request client
        Response<String> data = muon.request("request://myservice/", String.class).get();

        System.out.println("The Data is " + data.getPayload());
    }
}
