package com.simplicity.services;

import org.eclipse.jetty.util.ajax.JSON;
import org.muoncore.*;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import org.muoncore.extension.http.HttpTransportExtension;
import org.muoncore.transports.MuonMessageEvent;
import org.muoncore.transports.MuonResourceEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the Muon HTTP TCK Resources to prove compatibility of the library
 */
public class TCKService {

    public static void main(String[] args) {

        final Muon muon = new Muon();

        muon.setServiceIdentifer("tck");
        muon.registerExtension(new HttpTransportExtension(7171));
        muon.registerExtension(new AmqpTransportExtension());
        muon.start();

        final List events = Collections.synchronizedList(new ArrayList());

        muon.receive("tckBroadcast", new MuonService.MuonListener() {
            public void onEvent(MuonMessageEvent event) {
                events.add(JSON.parse(event.getPayload().toString()));
            }
        });

        muon.onGet("/event", "Get The Events", new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {
                return JSON.toString(events);
            }
        });
        muon.onDelete("/event", "Remove The Events", new MuonService.MuonDelete() {
            @Override
            public Object onCommand(MuonResourceEvent queryEvent) {
                events.clear();
                return "{}";
            }
        });

        muon.onGet("/echo", "Get Some Data", new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {
                Map obj = (Map) JSON.parse((String) queryEvent.getPayload());

                obj.put("method", "GET");

                return JSON.toString(obj);
            }
        });

        muon.onPost("/echo", "Allow posting of some data", new MuonService.MuonPost() {
            @Override
            public Object onCommand(MuonResourceEvent queryEvent) {
                Map obj = (Map) JSON.parse((String) queryEvent.getPayload());

                obj.put("method", "POST");

                return JSON.toString(obj);
            }
        });

        muon.onPut("/echo", "Allow posting of some data", new MuonService.MuonPut() {
            @Override
            public Object onCommand(MuonResourceEvent queryEvent) {
                //todo, something far more nuanced. Need to return a onGet url as part of the creation.

                Map obj = (Map) JSON.parse((String) queryEvent.getPayload());

                obj.put("method", "PUT");

                return JSON.toString(obj);
            }
        });

        muon.onDelete("/echo", "Allow deleting of some data", new MuonService.MuonDelete() {
            @Override
            public Object onCommand(MuonResourceEvent queryEvent) {

                Map obj = (Map) JSON.parse((String) queryEvent.getPayload());

                obj.put("method", "DELETE");

                return JSON.toString(obj);
            }
        });

        muon.onGet("/discover", "Show the currently discovered service identifiers.", new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {
                List<String> ids = new ArrayList<String>();

                for (ServiceDescriptor desc: muon.discoverServices()) {
                    ids.add(desc.getIdentifier());
                }

                return JSON.toString(ids);
            }
        });
    }
}
