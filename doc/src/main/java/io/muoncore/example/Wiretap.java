package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.message.MuonMessage;
import io.muoncore.protocol.rpc.server.RpcServer;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static io.muoncore.protocol.reactivestream.ProtocolMessages.REQUEST;
import static io.muoncore.protocol.rpc.server.HandlerPredicates.all;

public class Wiretap {

  public void exec(Muon muon) throws ExecutionException, InterruptedException, URISyntaxException, UnsupportedEncodingException {

    RpcServer rpc = new RpcServer(muon);

    // tag::setupRPC[]
    rpc.handleRequest(all(), request -> {
      request.ok(42);
    });
    // end::setupRPC[]

    // tag::wiretap[]
    Set<String> remoteServices = new HashSet<>();     // <1>
    Subscriber<MuonMessage> sub = new Subscriber<MuonMessage>() {
      @Override
      public void onSubscribe(Subscription s) { s.request(Long.MAX_VALUE); }

      @Override
      public void onNext(MuonMessage msg) {
        remoteServices.add(msg.getSourceServiceName());  // <2>
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onComplete() {

      }
    };

    muon.getTransportControl().tap(                      // <3>
      msg ->
        msg.getStep().equals(REQUEST))                   // <4>
      .subscribe(sub);
    // end::wiretap[]
  }
}
