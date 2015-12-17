package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.protocol.requestresponse.server.RequestWrapper;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all;

public class ReactiveRPCBatch {

    public void exec(Muon muon) throws ExecutionException, InterruptedException {

        Queue<RequestWrapper> requestQueue = new LinkedList<>();

        //request handler
        muon.handleRequest(all(), Object.class, requestQueue::add);


        //TODO, a good eaxmple of the pattern


    }
}
