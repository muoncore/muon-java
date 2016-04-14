package io.muoncore.example.event.guide;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.client.DefaultEventClient;
import io.muoncore.protocol.event.client.EventClient;

public class EmitEvent {

    public static void main(String[] args) {

        AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("client").build();

        Muon muon = MuonBuilder.withConfig(config).build();

        muon.getDiscovery().blockUntilReady();

        EventClient eventClient = new DefaultEventClient(muon);

        eventClient.event(new ClientEvent<>(
                "UserRegistered",
                "users",
                null,
                null,
                null,
                new UserRegisteredEvent(
                        "regsanders",
                        "Reginald",
                        "Sanders")
        ));

        eventClient.event(new ClientEvent<>(
                "UserRegistered",
                "users",
                null,
                null,
                null,
                new UserRegisteredEvent(
                        "derek",
                        "Derek",
                        "Blimby")
        ));

        eventClient.event(new ClientEvent<>(
                "UserDeleted",
                "users",
                null,
                null,
                null,
                new UserDeletedEvent("regsanders")
        ));

        System.out.println("User data updated");
        muon.shutdown();
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
