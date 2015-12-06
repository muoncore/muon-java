package com.simplicity.services.spring.server;

import io.muoncore.spring.annotations.EnableMuon;
import io.muoncore.spring.annotations.MuonController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

//@Configuration
@ComponentScan(basePackages = {"com.simplicity.services.spring.server"})
////@EnableMuon(serviceName = "${muon.server.name}",
////        tags = {"${muon.server.tag1}", "${muon.server.tag2}"},
////        aesEncryptionKey = "${muon.aesEncryptionKey}")
//@EnableMuon(serviceName = "reallySimple")
@PropertySource("classpath:application.properties")
//@SpringBootApplication
@SpringBootApplication
@MuonController
@EnableMuon(serviceName = "service1")
public class ServerApplication {
//
//@Bean
//    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
//        return new PropertySourcesPlaceholderConfigurer();
//    }
    public static void main(String[] args) {
        ApplicationContext ctx =
                SpringApplication.run(ServerApplication.class);
    }
}
