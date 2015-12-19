package io.muoncore.example.user

import io.muoncore.spring.annotations.EnableMuon
import io.muoncore.spring.annotations.MuonController
import io.muoncore.spring.annotations.MuonRequestListener
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/*
@Grapes([
        @Grab('io.muoncore:muon-transport-amqp:6.4-SNAPSHOT'),
        @Grab('io.muoncore:muon-discovery-amqp:6.4-SNAPSHOT'),
        @Grab('io.muoncore:muon-spring:6.4-SNAPSHOT')])
*/
@SpringBootApplication
@EnableMuon(serviceName = "users")   // <2>
@MuonController                      // <3>
class UserService {

    @MuonRequestListener(path = "/")   //<3>
    def myRpcEndpoint(Map data) {
        return [message:"from uer service ${System.currentTimeMillis()}"]
    }
    public static void main(String[] args) throws Exception {
        SpringApplication.run(UserService, args);
    }
}
