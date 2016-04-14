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

/**
 * Created by david on 14/04/16.
 */
public class UserAggregationService {

    //subscribe to the user stream
    // fold into a user account

    public static void main(String[] args) {

        AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("user-service").build();

        Muon muon = MuonBuilder.withConfig(config).build();

        muon.getDiscovery().blockUntilReady();

        EventClient evclient = new DefaultEventClient(muon);

        Set<String> usernames = new HashSet<>();

        Broadcaster<Event<Map>> sub = Broadcaster.create();
        sub.consume(msg -> {
            try {
                if (msg.getEventType().equals("UserRegistered")) {
                    usernames.add((String) msg.getPayload().get("username"));
                } else {
                    usernames.remove(msg.getPayload().get("username"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("Processed event, user list is now : " + usernames);
        });

        evclient.replay("users", EventReplayMode.REPLAY_THEN_LIVE, Map.class, sub);

    }

    static class UserRegisteredEvent {
        private String username;
        private String firstname;
        private String lastname;

        public UserRegisteredEvent(String username, String firstname, String lastname) {
            this.username = username;
            this.firstname = firstname;
            this.lastname = lastname;
        }

        public String getUsername() {
            return username;
        }

        public String getFirstname() {
            return firstname;
        }

        public String getLastname() {
            return lastname;
        }
    }

    static class UserDeletedEvent {
        private String username;

        public UserDeletedEvent(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }
}