package io.muoncore.example.event.guide;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.client.DefaultEventClient;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventReplayMode;
import reactor.rx.broadcast.Broadcaster;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserAggregationService {

    public static void main(String[] args) {

        AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("user-service").build();

        Muon muon = MuonBuilder.withConfig(config).build();

        muon.getDiscovery().blockUntilReady();

        EventClient evclient = new DefaultEventClient(muon);

        Set<String> usernames = new HashSet<>();

        Broadcaster<Event> sub = Broadcaster.create();
        sub.consume(msg -> {
            try {
                if (msg.getEventType().equals("UserRegistered")) {
                    usernames.add((String) msg.getPayload(Map.class).get("username"));
                } else {
                    usernames.remove(msg.getPayload(Map.class).get("username"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("Processed event, user list is now : " + usernames);
        });

        evclient.replay("users", EventReplayMode.REPLAY_THEN_LIVE, sub);

    }
}
