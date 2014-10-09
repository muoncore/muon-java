package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.MuonClient;
import org.muoncore.MuonService;

public class Service2 {

    public static void main(String[] args) {

        final MuonClient muon = new Muon();

//        muon.emit("something", "Be Happy");

        String myData = muon.get("muon://users/mydata/happy").getEvent().getPayload().toString();

        System.out.println("The data is " + myData);
    }
}
