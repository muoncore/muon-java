package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.protocol.reactivestream.client.ReactiveStreamClient;
import io.muoncore.protocol.reactivestream.client.StreamData;
import io.muoncore.protocol.reactivestream.server.PublisherLookup;
import io.muoncore.protocol.reactivestream.server.ReactiveStreamServer;
import io.muoncore.protocol.rpc.Response;
import io.muoncore.protocol.rpc.client.RpcClient;
import io.muoncore.protocol.rpc.server.HandlerPredicates;
import io.muoncore.protocol.rpc.server.RpcServer;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static io.muoncore.protocol.reactivestream.server.PublisherLookup.PublisherType.COLD;


public class ReactiveStreams {

  static Subscriber subscriber = null;

  // tag::server[]
  public void server(Muon muon) throws ExecutionException, InterruptedException {
    ReactiveStreamServer rxServer = new ReactiveStreamServer(muon);                  // <1>

    rxServer.publishSource("/counter", COLD, Flowable.range(5, 10)); // <2>

    rxServer.publishGeneratedSource("/dynamic-counter", COLD, subscriptionRequest -> {         // <3>

      int start = Integer.parseInt(subscriptionRequest.getArgs().getOrDefault("counter", "5")); // <4>

      return Flowable.range(start, start + 100);        // <5>
    });
  }
  // end::server[]

  // tag::client[]
  public void client(Muon muon) throws URISyntaxException {
    ReactiveStreamClient rxClient = new ReactiveStreamClient(muon);

    rxClient.subscribe(new URI("stream://my-server/counter"), new Subscriber<StreamData>() {  // <1>
      @Override
      public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);                // <2>
      }

      @Override
      public void onNext(StreamData streamData) {  // <3>
        Map payload = streamData.getPayload(Map.class);

        //do something with the data ..
        System.out.println(payload);
      }

      @Override
      public void onError(Throwable t) {
        t.printStackTrace();
      }

      @Override
      public void onComplete() {
        // stream is done. clean up resources.
      }
    });

    rxClient.subscribe(new URI("stream://my-server/dynamic-counter?counter=30"),   // <4>
      subscriber
    );

  }
  // end::client[]
}
