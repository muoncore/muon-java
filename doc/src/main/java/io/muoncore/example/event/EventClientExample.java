package io.muoncore.example.event;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.client.DefaultEventClient;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventReplayMode;
import reactor.rx.broadcast.Broadcaster;

import java.util.HashSet;
import java.util.Set;

public class EventClientExample {

    public static void main(String[] args) {

        AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("client").build();

        Muon muon = MuonBuilder.withConfig(config).build();

        muon.getDiscovery().blockUntilReady();

        // tag::createclient[]
        EventClient evclient = new DefaultEventClient(muon);
        // end::createclient[]

        // tag::replay[]
        Broadcaster<Event> sub = Broadcaster.create();
        sub.consume( msg -> {
//            println "EVENT = ${it}"

        });

        evclient.replay("users", EventReplayMode.REPLAY_THEN_LIVE, sub);
        // end::replay[]


        // tag::emitevent[]
        evclient.event(
                ClientEvent.ofType("UserRegistered")
                        .payload(new UserRegistered("Roger", "Rabbit"))
                        .stream("users")
                        .build());

        // end::emitevent[]

        // tag::eventsource[]

        Set<String> userList = new HashSet<>();

        Broadcaster<Event> eventsourceSubscriber = Broadcaster.create();
        eventsourceSubscriber.consume( event -> {
            System.out.printf("User was registered %s %s", event.getPayload(UserRegistered.class).getFirstname(), event.getPayload(UserRegistered.class).getLastname());
            userList.add(event.getPayload(UserRegistered.class).getFirstname() + " " + event.getPayload(UserRegistered.class).getLastname());
        });

        evclient.replay("users", EventReplayMode.REPLAY_THEN_LIVE, eventsourceSubscriber);

        // end::eventsource[]
    }

    static class UserRegistered {
        private String firstname;
        private String lastname;

        public UserRegistered(String firstname, String lastname) {
            this.firstname = firstname;
            this.lastname = lastname;
        }

        public String getFirstname() {
            return firstname;
        }

        public String getLastname() {
            return lastname;
        }
    }
}
