package com.simplicity.services;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all;

public class ServiceAsapConsumer {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException {

        AutoConfiguration config = MuonConfigBuilder
                .withServiceIdentifier("awesomeService")
                .withTags("node", "awesome")
                .build();

        Muon muon = MuonBuilder.withConfig(config).build();

        //allow discovery settle time.
        Thread.sleep(5000);

        muon.handleRequest(all(), Map.class, response -> {
            response.ok("Hellow");
        });
    }
}
