package io.muoncore.example.springexample;

import io.muoncore.spring.annotations.EnableMuon;
import io.muoncore.spring.annotations.MuonController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// tag::autowire[]
@SpringBootApplication
@EnableMuon(serviceName = "myService")
@MuonController
public class BasicMuon {

    public static void main(String[] args) {
        SpringApplication.run(BasicMuon.class, args);
    }
}
// end::autowire[]