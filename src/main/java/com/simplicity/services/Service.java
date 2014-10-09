package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.MuonBroadcastEvent;
import org.muoncore.MuonResourceEvent;
import org.muoncore.MuonService;
import org.muoncore.extension.eventlogger.EventLoggerExtension;
import org.muoncore.extension.introspection.IntrospectionExtension;
import static org.muoncore.MuonResourceEventBuilder.*;
import static org.muoncore.MuonBroadcastEventBuilder.*;

public class Service {

    public static void main(String[] args) {

        MuonService muon = new Muon();

        muon.setServiceIdentifer("users");

        muon.registerExtension(new IntrospectionExtension());
        muon.registerExtension(new EventLoggerExtension());

        muon.receive("email", new MuonService.MuonListener() {
            @Override
            public void onEvent(MuonBroadcastEvent event) {
                System.out.println("GO AN EMAIL!" + event.getPayload());
                System.out.println("To ... " + event.getHeaders().get("to"));
            }
        });

//        muon.emit("email",
//                broadcast("Hello Everyone, this is my awesome email")
//                        .withHeader("to", "david.dawson@simplicityitself.com")
//                        .withHeader("from", "muon@simplicityitself.com")
//                        .build());

        muon.resource("mydata", "Get Some Data", new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {
                return "<h1> This is awesome!!</h1>";
            }
        });
    }
}
