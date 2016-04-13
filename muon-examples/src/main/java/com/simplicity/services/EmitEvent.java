package com.simplicity.services;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.client.DefaultEventClient;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventResult;

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

        EventClient client = new DefaultEventClient(muon);

        Thread.sleep(2000);

        Map data = new HashMap<>();
        data.put("hello", "world");

        for(int i=0; i < 500; i++ ) {
            EventResult res = client.event(new ClientEvent("awesome", "awesome", "1.0", 12313l, "causedby",  data));
            System.out.println("Restul is " + res.getStatus() + " " + res.getCause());
        }

        muon.shutdown();
    }
}
