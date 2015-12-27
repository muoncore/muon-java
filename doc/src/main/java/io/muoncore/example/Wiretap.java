package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.protocol.requestresponse.RRPEvents;
import io.muoncore.transport.TransportMessage;
import reactor.rx.broadcast.Broadcaster;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all;

public class Wiretap {

    public void exec(Muon muon) throws ExecutionException, InterruptedException, URISyntaxException, UnsupportedEncodingException {

        // tag::setupRPC[]
        Broadcaster<String> publisher = Broadcaster.create();

        muon.handleRequest(all(), Map.class, request -> {
            request.ok(42);
        });

        // end::setupRPC[]

        // tag::wiretap[]
        Set<String> remoteServices = new HashSet<>();     // <1>
        Broadcaster<TransportMessage> requests = Broadcaster.create();

        requests.consume(msg -> {
            remoteServices.add(msg.getSourceServiceName());  //<2>
        });

        muon.getTransportControl().tap(                      //<3>
                msg -> msg.getType().equals(RRPEvents.REQUEST)).subscribe(requests); //<4>
        // end::wiretap[]

        // tag::wiretap2[]
        Broadcaster<TransportMessage> responses = Broadcaster.create();

        responses.consume(msg -> {
            System.out.println("Sent a response to " + msg.getTargetServiceName());
        });

        muon.getTransportControl().tap(
                msg -> msg.getType().equals(RRPEvents.RESPONSE)).subscribe(responses);
        // end::wiretap2[]

        // tag::fireRPC[]
        int value = muon.request("request://myservice/", Integer.class).get().getPayload();
        // end::fireRPC[]
    }
}
