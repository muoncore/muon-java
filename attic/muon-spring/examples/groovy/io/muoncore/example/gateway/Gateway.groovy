package io.muoncore.example.gateway

import io.muoncore.Muon
import io.muoncore.spring.annotations.EnableMuon
import io.muoncore.spring.annotations.MuonController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody;
/*
@Grapes([
        @Grab('io.muoncore:muon-transport-amqp:6.4-SNAPSHOT'),
        @Grab('io.muoncore:muon-discovery-amqp:6.4-SNAPSHOT'),
        @Grab('io.muoncore:muon-spring:6.4-SNAPSHOT')])
*/
@SpringBootApplication
@Controller
@MuonController
@EnableMuon(serviceName = "gateway")
class Gateway {

    @Autowired                     //<1>
    Muon muon

    @RequestMapping("/")           //<2>
    @ResponseBody
    String home() {
        muon.request("request://users/", Map).get().payload   //<3>
    }
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Gateway, args);
    }
}


