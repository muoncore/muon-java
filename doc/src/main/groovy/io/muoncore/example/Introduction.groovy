package io.muoncore.example

import io.muoncore.spring.annotations.EnableMuon
import io.muoncore.spring.annotations.MuonController
import io.muoncore.spring.annotations.MuonRequestListener
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
@MuonController                        //(1) Enable Muon and set up
@EnableMuon(serviceName = "tombola")   //(2)
class Introduction {

    @MuonRequestListener(path = "/")   //(3) An RPC Endpoint
    def myRpcEndpoint(Map data) {
        return data
    }
}


