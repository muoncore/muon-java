package com.simplicity.services;

import org.muoncore.MuonService;
import org.muoncore.TransportedMuon;

public class Service2 {

    public static void main(String[] args) {

        final MuonService muon = new TransportedMuon();

        muon.emit("something", "Be Happy");

        String myData = muon.get("/mydata").getEvent().toString();

        System.out.println("The data is " + myData);
    }
}
