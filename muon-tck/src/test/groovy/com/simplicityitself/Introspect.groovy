package com.simplicityitself

import groovy.json.JsonBuilder
import io.muoncore.Muon
import io.muoncore.spring.annotations.EnableMuon
import io.muoncore.spring.annotations.MuonController
import io.muoncore.spring.repository.MuonEventStoreRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@EnableMuon(serviceName="exampleApplication")
@MuonController
class Introspect {

    @Autowired MuonEventStoreRepository eventStoreRepository;

    static void main(def args) {
        def muon = SpringApplication.run(Introspect).getBean(Muon)

        Thread.sleep(4000)

//        println new JsonBuilder(muon.request("request://UserService/addPerson", [
//                name:"Gawain",
//                age:25
//        ], Map).get()).toPrettyString()
//
        println new JsonBuilder(muon.request("request://UserService/getPeople", Object).get()).toPrettyString()


//        println new JsonBuilder(muon.discovery.knownServices).toPrettyString()
//        println new JsonBuilder(muon.introspect("service1").get()).toPrettyString()
//        println new JsonBuilder(muon.request("request://service1/getPeople", List).get().payload).toPrettyString()

        muon.shutdown()
        println "DONE!"
        Thread.sleep(2000)

    }
}
