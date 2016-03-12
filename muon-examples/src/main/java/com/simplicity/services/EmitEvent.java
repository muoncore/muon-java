package com.simplicity.services;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.api.MuonFuture;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.event.Event;
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

        Thread.sleep(2000);

        Map data = new HashMap<>();
        data.put("hello", "world");

        for(int i=0; i < 500; i++ ) {
            MuonFuture<EventResult> res = muon.getEventStoreClient().event(new Event<>("awesome", "123", null, config.getServiceName(), data));
            EventResult result = res.get();
            System.out.println("Restul is " + result.getStatus() + " " + result.getCause());
        }

        muon.shutdown();
    }
}
