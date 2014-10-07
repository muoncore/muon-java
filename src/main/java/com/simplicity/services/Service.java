package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.TransportedMuon;
import org.muoncore.extension.eventlogger.EventLoggerExtension;
import org.muoncore.extension.router.RouterExtension;

import java.util.Collections;
import java.util.List;

public class Service {

    public static void main(String[] args) {

        Muon muon = new TransportedMuon();

        muon.receive("something", new Muon.MuonListener() {
            @Override
            public void onEvent(Object event) {
                System.out.println("Hello World " + event);
            }
        });

        muon.resource("/mydata", "Get Some Data", new Muon.MuonGet() {
            @Override
            public Object onQuery(Object queryEvent) {
                return "<h1> This is awesome!!</h1>";
            }
        });
    }
}
