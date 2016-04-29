package com.simplicity.services;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.ServiceDescriptor;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.reactivestream.server.PublisherLookup;
import io.muoncore.protocol.requestresponse.Response;
import org.reactivestreams.Publisher;
import reactor.rx.Streams;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.path;

/**
 * An implementation of the Muon HTTP TCK Resources to prove compatibility of the library
 */
public class TCKService {

    public static void main(String[] args) throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException, InterruptedException {

        String serviceName = "tckservice";

        AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier(serviceName).build();

        Muon muon = MuonBuilder.withConfig(config).build();

        muon.getDiscovery().blockUntilReady();

        outboundResourcesSetup(muon);

        inboundResourcesSetup(muon);

        streamPublisher(muon);

    }

    private static void outboundResourcesSetup(final Muon muon) {

        final Map storedata = new HashMap();

        muon.handleRequest(path("/invokeresponse-store"), queryEvent -> queryEvent.ok(storedata) );

        muon.handleRequest(path("/invokeresponse"), queryEvent -> {

                String url = (String) queryEvent.getRequest().getPayload(Map.class).get("resource");

                Response result = null;
                try {
                    result = muon.request(url, Map.class).get();
                    storedata.clear();
                    storedata.putAll(result.getPayload(Map.class));
                    queryEvent.ok(result.getPayload(Map.class));
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        );
    }

    private static void streamPublisher(Muon muon) {
        Publisher<Long> pub = Streams.range(1, 10);
        muon.publishSource("/myStream", PublisherLookup.PublisherType.COLD, pub);
    }

    private static void inboundResourcesSetup(final Muon muon) {
        muon.handleRequest(path("/echo"), queryEvent -> {
                Map obj = queryEvent.getRequest().getPayload(Map.class);

                obj.put("method", "GET");

                queryEvent.ok(obj);
            });

        muon.handleRequest(path("/discover"), request ->
                request.ok(
                        muon.getDiscovery().getKnownServices().stream().map(ServiceDescriptor::getIdentifier).collect(Collectors.toList())));
    }

}
