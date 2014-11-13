package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.MuonClient;
import org.muoncore.extension.amqp.AmqpTransportExtension;

public class Service2 {

    public static void main(String[] args) {

        final Muon muon = new Muon();

        muon.registerExtension(new AmqpTransportExtension());
        muon.start();

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
