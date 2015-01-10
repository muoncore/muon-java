package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.MuonService;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import org.muoncore.transports.MuonResourceEvent;

public class GetUser {

    public static void main(String[] args) {

        MuonService muon = new Muon();
        muon.registerExtension(new AmqpTransportExtension());

        muon.setServiceIdentifer("userchecker");
        muon.start();

        String data = muon.get("muon://users/mydata/happy").getResponseEvent().getPayload().toString();

        System.out.println ("We had data " + data);
    }
}
