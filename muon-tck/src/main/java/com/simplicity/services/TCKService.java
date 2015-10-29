package com.simplicity.services;

import io.muoncore.*;
import io.muoncore.config.MuonBuilder;
import io.muoncore.crud.MuonClient;
import io.muoncore.crud.MuonService;
import io.muoncore.crud.OldMuon;
import io.muoncore.future.MuonFuture;
import io.muoncore.future.MuonFutures;
import io.muoncore.transport.crud.requestresponse.MuonResourceEvent;
import org.reactivestreams.Publisher;
import reactor.rx.Streams;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;



/**
 * An implementation of the Muon HTTP TCK Resources to prove compatibility of the library
 */
public class TCKService {

    public static void main(String[] args) throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException {

        final OldMuon muon = new MuonBuilder()
                .withServiceIdentifier("tck")
                .withTags("my-tag", "tck-service")
                .build();

        muon.start();

        outboundResourcesSetup(muon);

        inboundResourcesSetup(muon);

        streamPublisher(muon);

    }

    private static void outboundResourcesSetup(final OldMuon muon) {

        final Map storedata = new HashMap();

        muon.onQuery("/invokeresponse-store", Map.class, queryEvent -> MuonFutures.immediately(storedata));

        muon.onCommand("/invokeresponse", Map.class, queryEvent -> {

                    String url = (String) queryEvent.getDecodedContent().get("resource");

                    MuonFuture<MuonClient.MuonResult<Map>> rsult = muon.query(url, Map.class);

                    return MuonFutures.fromPublisher(
                            Streams.wrap(rsult.toPublisher()).map(mapMuonResult -> {
                                Map data;
                                data = mapMuonResult.getResponseEvent().getDecodedContent();
                                storedata.clear();
                                storedata.putAll(data);

                                return data;
                            }));
                }
        );
    }

    private static void streamPublisher(OldMuon muon) {
        Publisher<Long> pub = Streams.range(1, 10);
        muon.streamSource("/myStream", Long.class, pub);
    }

    private static void inboundResourcesSetup(final OldMuon muon) {
        muon.onQuery("/echo", Map.class, new MuonService.MuonQueryListener<Map>() {
            @Override
            public MuonFuture onQuery(MuonResourceEvent<Map> queryEvent) {
                Map obj = queryEvent.getDecodedContent();

                obj.put("method", "GET");

                return MuonFutures.immediately(obj);
            }
        });

        muon.onCommand("/echo", Map.class, new MuonService.MuonCommandListener<Map>() {
            @Override
            public MuonFuture<Map> onCommand(MuonResourceEvent<Map> queryEvent) {
                String method = queryEvent.getHeaders().get("METHOD");

                Map obj = queryEvent.getDecodedContent();

                obj.put("method", method);

                return MuonFutures.immediately(obj);
            }
        });

        muon.onQuery("/discover", Map.class, new MuonService.MuonQueryListener<Map>() {
            @Override
            public MuonFuture<List<String>> onQuery(MuonResourceEvent<Map> queryEvent) {
                List<String> ids = new ArrayList<String>();

                for (ServiceDescriptor desc: muon.discoverServices()) {
                    ids.add(desc.getIdentifier());
                }

                return MuonFutures.immediately(ids);
            }
        });
    }

}
