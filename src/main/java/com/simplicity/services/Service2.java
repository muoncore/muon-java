package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.MuonClient;

public class Service2 {

    public static void main(String[] args) {

        final MuonClient muon = new Muon();


        String myData = muon.get("muon://tck/echo").getResponseEvent().getPayload().toString();



//        String userData = muon.get("muon://users/mydata/happy").getResponseEvent().getPayload().toString();

        System.out.println("The data is " + myData);
//        System.out.println("The data is " + userData);
//
//        muon.emit(broadcast("email")
//                    .withContent("Hello Everyone, this is my awesome email")
//                    .withHeader("to", "david.dawson@simplicityitself.com")
//                    .withHeader("from", "asap@simplicityitself.com")
//                    .build());

        muon.shutdown();
    }
}
