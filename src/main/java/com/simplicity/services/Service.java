package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.MuonService;
import org.muoncore.extension.introspection.IntrospectionExtension;
import static org.muoncore.MuonEventBuilder.*;

public class Service {

    public static void main(String[] args) {

        MuonService muon = new Muon();

        muon.setServiceIdentifer("users");

        muon.registerExtension(new IntrospectionExtension());

        muon.receive("something", new MuonService.MuonListener() {
            @Override
            public void onEvent(Object event) {
                System.out.println("Hello World " + event);
            }
        });

        muon.emit("email",
                textMessage("Hello Everyone, this is my awesome email")
                        .withHeader("to", "david.dawson@simplicityitself.com")
                        .withHeader("from", "muon@simplicityitself.com")
                        .build());

        muon.resource("mydata", "Get Some Data", new MuonService.MuonGet() {
            @Override
            public Object onQuery(Object queryEvent) {
                return "<h1> This is awesome!!</h1>";
            }
        });
    }
}
