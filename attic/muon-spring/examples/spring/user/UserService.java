package com.simplicity.services.example.user;

import io.muoncore.Muon;
import io.muoncore.protocol.rpc.client.requestresponse.server.RequestWrapper;
import io.muoncore.spring.annotations.EnableMuon;
import io.muoncore.spring.annotations.MuonRequestListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.Environment;
import reactor.rx.broadcast.Broadcaster;

import javax.annotation.PostConstruct;

import static io.muoncore.protocol.reactivestream.server.PublisherLookup.PublisherType.HOT;

@SpringBootApplication
@EnableMuon(serviceName = "UserService")
@Configuration
@EnableScheduling
public class UserService {

    @Autowired
    private Muon muon;
    private Broadcaster<String> tickTock = Broadcaster.create(Environment.initializeIfEmpty());

    @PostConstruct
    public void setupStreams() {
        muon.publishSource("tickTock", HOT, tickTock);
    }

    @Scheduled(fixedRate = 5000l)
    public void emitData() {
        System.out.println("Sending data");
        tickTock.accept("Hello " + System.currentTimeMillis());
    }

    @MuonRequestListener(path = "/")
    public void doSomething(RequestWrapper wrapper) {
        wrapper.ok("Hello there");
    }

    public static void main(String[] args) {
        SpringApplication.run(UserService.class);
    }
}
