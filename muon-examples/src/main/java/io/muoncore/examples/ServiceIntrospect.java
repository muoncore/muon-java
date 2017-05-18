package io.muoncore.examples;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.api.MuonFuture;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.descriptors.ServiceExtendedDescriptor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

public class ServiceIntrospect {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException, ExecutionException {

        String serviceName = "awesomeServiceQuery";

        AutoConfiguration config = MuonConfigBuilder
                .withServiceIdentifier(serviceName)
                .withTags("node", "awesome")
                .build();

        Muon muon = MuonBuilder.withConfig(config).build();

        //allow discovery settle time.
        Thread.sleep(5000);

        MuonFuture<ServiceExtendedDescriptor> desc = muon.introspect("photon");

        desc.get().getProtocols().stream().forEach(proto -> {
                System.out.println("PROTOCOL: " + proto.getProtocolName());
                proto.getOperations().stream().forEach(op -> {
                    System.out.println(op.getResource());
                });
            });

        muon.shutdown();
    }
}
