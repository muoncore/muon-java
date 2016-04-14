package io.muoncore.example.event.guide;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.event.client.DefaultEventClient;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventProjectionControl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class UserProjectionService {

    //add the projection into photon

    //read the projection on demand

    //read part of the projection for a single account?

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("user-service-projection").build();

        Muon muon = MuonBuilder.withConfig(config).build();

        muon.getDiscovery().blockUntilReady();

        EventClient evclient = new DefaultEventClient(muon);

        EventProjectionControl<Map> userList = evclient.getProjection("user-list", Map.class).get();

        Set users = ((Map) userList.getCurrentState().get("current-value")).keySet();

        System.out.println ("Users are " + users);

        muon.shutdown();
    }
}
