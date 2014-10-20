package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.MuonBroadcastEvent;
import org.muoncore.MuonResourceEvent;
import org.muoncore.MuonService;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import org.muoncore.extension.eventlogger.EventLoggerExtension;
import org.muoncore.extension.http.HttpTransportExtension;
import org.muoncore.extension.introspection.IntrospectionExtension;
import static org.muoncore.MuonResourceEventBuilder.*;
import static org.muoncore.MuonBroadcastEventBuilder.*;

/**
 * An implementation of the Muon HTTP TCK Resources to prove compatibility of the library
 */
public class TCKService {

    public static void main(String[] args) {

        MuonService muon = new Muon();

        muon.setServiceIdentifer("tck");
        muon.registerExtension(new HttpTransportExtension(7171));
//        muon.registerExtension(new AmqpTransportExtension());
        muon.start();


        muon.receive("myevent", new MuonService.MuonListener() {
            @Override
            public void onEvent(MuonBroadcastEvent event) {

            }
        });

        muon.resource("/echo", "Get Some Data", new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {
                return queryEvent.getPayload();
            }
        });

        muon.resource("/echo", "Allow posting of some data", new MuonService.MuonPost() {
            @Override
            public Object onCommand(MuonResourceEvent queryEvent) {
                return queryEvent.getPayload();
            }
        });

        muon.resource("/echo", "Allow posting of some data", new MuonService.MuonPut() {
            @Override
            public Object onCommand(MuonResourceEvent queryEvent) {
                //todo, something far more nuanced. Need to return a resource url as part of the creation.

                return queryEvent.getPayload();
            }
        });
    }
}
