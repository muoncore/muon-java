package io.muoncore.example;

import io.muoncore.Muon;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class ReactiveRPCAsync {

    public void exec(Muon muon) throws ExecutionException, InterruptedException {

//        Queue<RequestWrapper> requestQueue = new LinkedList<>();
//
//        //request handler
//        muon.handleRequest(all(), requestQueue::add);
//
//        new Thread(() -> {
//
//            while(true) {
//
//                RequestWrapper wrapper = requestQueue.poll();
//
//                wrapper.ok("Hello");
//
//                if (requestQueue.isEmpty()) {
//                    // ... wait ....
//                }
//            }
//        });
//
//        //request client
//        Response data = muon.request("request://myservice/").get();
//
//        System.out.println("The Data is " + data.getPayload(String.class));
    }
}
