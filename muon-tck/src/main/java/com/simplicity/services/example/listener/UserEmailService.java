package com.simplicity.services.example.listener;

import io.muoncore.spring.annotations.EnableMuon;
import io.muoncore.spring.annotations.MuonController;
import io.muoncore.spring.annotations.MuonStreamListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@EnableMuon(serviceName = "UserListener")
@Configuration
@MuonController
public class UserEmailService {

    @MuonStreamListener(url = "stream://UserService/tickTock")
    public void listenToData(String data) {
        System.out.println("The data is " + data);
    }

    public static void main(String[] args) {
        SpringApplication.run(UserEmailService.class);
    }
}
