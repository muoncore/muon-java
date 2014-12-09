package com.simplicity.services;

import org.eclipse.jetty.util.ajax.JSON;
import org.muoncore.*;
import org.muoncore.extension.amqp.AmqpTransportExtension;

public class Service2 {

    public static void main(String[] args) {

        final Muon muon = new Muon();

        muon.registerExtension(new AmqpTransportExtension());
        muon.start();

        System.out.println("Saying hello there!");

        muon.receive("tckBroadcast", new MuonService.MuonListener() {
            @Override
            public void onEvent(MuonBroadcastEvent event) {
                System.out.println("Got tckBroadcast " + event.getPayload().toString());
            }
        });

        muon.emit(MuonBroadcastEventBuilder.broadcast("simples").withContent("{}").build());



        String myData = muon.get("muon://tck/event").getResponseEvent().getPayload().toString();




//        String userData = muon.get("muon://users/mydata/happy").getResponseEvent().getPayload().toString();

//        System.out.println("The data is " + myData);
//        System.out.println("The data is " + userData);
//
//        muon.emit(broadcast("email")
//                    .withContent("Hello Everyone, this is my awesome email")
//                    .withHeader("to", "david.dawson@simplicityitself.com")
//                    .withHeader("from", "asap@simplicityitself.com")
//                    .build());

//        muon.shutdown();
    }
}
