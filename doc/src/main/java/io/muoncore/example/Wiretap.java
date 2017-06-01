package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.message.MuonMessage;
import reactor.rx.broadcast.Broadcaster;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class Wiretap {

    public void exec(Muon muon) throws ExecutionException, InterruptedException, URISyntaxException, UnsupportedEncodingException {

        // tag::setupRPC[]
        Broadcaster<String> publisher = Broadcaster.create();

//        muon.handleRequest(all(), request -> {
//            request.ok(42);
//        });

        // end::setupRPC[]

        // tag::wiretap[]
//        Set<String> remoteServices = new HashSet<>();     // <1>
//        Broadcaster<MuonMessage> requests = Broadcaster.create();
//
//        requests.consume(msg -> {
//            remoteServices.add(msg.getSourceServiceName());  //<2>
//        });
//
//        muon.getTransportControl().tap(                      //<3>
//                msg -> msg.getStep().equals(RRPEvents.REQUEST)).subscribe(requests); //<4>
//        // end::wiretap[]
//
//        // tag::wiretap2[]
//        Broadcaster<MuonMessage> responses = Broadcaster.create();
//
//        responses.consume(msg -> {
//            System.out.println("Sent a response to " + msg.getTargetServiceName());
//        });
//
//        muon.getTransportControl().tap(
//                msg -> msg.getStep().equals(RRPEvents.RESPONSE)).subscribe(responses);
//        // end::wiretap2[]
//
//        // tag::fireRPC[]
//        int value = muon.request("request://myservice/").get().getPayload(Integer.class);
//        // end::fireRPC[]
    }
}
