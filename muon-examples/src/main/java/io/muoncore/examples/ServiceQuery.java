package io.muoncore.examples;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ServiceQuery {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException, ExecutionException, TimeoutException {

        String serviceName = "awesomeServiceQuery";

        AutoConfiguration config = MuonConfigBuilder
                .withServiceIdentifier(serviceName)
                .withTags("node", "awesome")
                .build();

        Muon muon = MuonBuilder.withConfig(config).build();

        Map data = muon.request("rpc://tckservice/echo").get().getPayload(Map.class);

        System.out.println("Data is " + data);

/*        EventClient eventClient = new DefaultEventClient(muon);
        //allow discovery settle time.
        Thread.sleep(5000);

        Map data = new HashMap<>();

//        MuonFuture<EventProjection<Map>> projection = eventClient.getProjection("SimpleProjection", Map.class);

        Response ret = muon.request("request://photon", data, String.class).get();

        System.out.println("Server responds " + ret.getPayload());*/
        muon.shutdown();
    }
}
