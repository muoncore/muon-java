package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.TransportedMuon;
import org.muoncore.extension.router.RouterExtension;

import java.util.Collections;
import java.util.List;

public class Service {

    public static void main(String[] args) {

        final StockManager stockRepository = new StockManager() {
            @Override
            public List top10ByQuantity() {
                return Collections.emptyList();
            }
        };


        final Muon muon = new TransportedMuon();
        muon.registerExtension(new RouterExtension());

        muon.receive("sendMail", new Muon.MuonListener() {
            @Override
            public void onEvent(Object event) {
                System.out.println("Hello World, got " + event);
            }
        });

        muon.resource("/stocks/top10/quantity", "The Top 10 Stocks, by traded quantity", new Muon.MuonGet() {
            @Override
            public Object onQuery(Object queryEvent) {
                return stockRepository.top10ByQuantity();
            }
        });
    }








    interface StockManager {
        List top10ByQuantity();
    }
}
