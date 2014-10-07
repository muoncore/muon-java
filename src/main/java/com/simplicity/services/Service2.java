package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.TransportedMuon;

public class Service2 {

    public static void main(String[] args) {

        final Muon muon = new TransportedMuon();

//        muon.emit("sendMail", "WIBBLE MONKEY");
        Muon.MuonResult result = muon.get("/muon/router");

        System.out.println("Result == " + result.getEvent());
    }
}
