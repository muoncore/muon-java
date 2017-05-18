package io.muoncore.examples;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.api.PromiseFunction;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.client.DefaultEventClient;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventReplayMode;
import io.muoncore.protocol.event.client.EventResult;
import io.muoncore.protocol.reactivestream.client.StreamData;
import reactor.rx.broadcast.Broadcaster;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class EmitEvent {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException, ExecutionException {

        AutoConfiguration config = MuonConfigBuilder
                .withServiceIdentifier("awesomeService")
                .withTags("node", "awesome")
                .build();


        Muon muon = MuonBuilder.withConfig(config).build();

        muon.getDiscovery().blockUntilReady();

        EventClient client = new DefaultEventClient(muon);

        Map data = new HashMap<>();
        data.put("hello", "world");

        long then = System.currentTimeMillis();

        long ratePerSecond = 20;

        subscribe(client);

        Thread.sleep(1000);
        while(true) {

          for (int i = 0; i < ratePerSecond; i++) {

            client.<Map>eventAsync(
              ClientEvent.ofType("SomethingHappened")
                .schema("17")
                .causedBy(123456789L, "Fully Qualified Bigness")
                .stream("hammer").payload(data).build()

            ).then((PromiseFunction<EventResult>) arg -> System.out.println("Restul is " + arg.getStatus() + " " + arg.getCause()));
//            System.out.println("Restul is " + res.getStatus() + " " + res.getCause());
//            System.out.println("Restul is " + res.getEventTime() + " " + res.getOrderId());
          }
          System.out.println("Sleeping .... ");
          Thread.sleep(1000);
        }
    }

    static void subscribe(EventClient cl) {
      Broadcaster<Event> v = Broadcaster.create();

      v.consume(o -> {
        System.out.println("Got data " + o);
      });

      cl.replay("hammer", EventReplayMode.LIVE_ONLY, v);
    }
}
