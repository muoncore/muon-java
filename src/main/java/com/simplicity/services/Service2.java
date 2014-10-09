package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.MuonClient;
import static org.muoncore.MuonBroadcastEventBuilder.*;

public class Service2 {

    public static void main(String[] args) {

        final MuonClient muon = new Muon();

        String myData = muon.get("muon://users/mydata/happy").getEvent().getPayload().toString();

        System.out.println("The data is " + myData);

        muon.emit(broadcast("email")
                    .withContent("Hello Everyone, this is my awesome email")
                    .withHeader("to", "david.dawson@simplicityitself.com")
                    .withHeader("from", "muon@simplicityitself.com")
                    .build());

        muon.shutdown();
    }
}
