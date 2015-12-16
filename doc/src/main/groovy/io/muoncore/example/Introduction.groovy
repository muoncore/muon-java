package io.muoncore.example

import io.muoncore.spring.annotations.EnableMuon
import io.muoncore.spring.annotations.MuonController
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
@MuonController                        //(1) Enable Muon and set up
@EnableMuon(serviceName = "tombola")   //(2)
class Introduction {


    def myRpcEndpoint() {

    }


}


