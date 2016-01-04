package io.muoncore.example.springexample;

import io.muoncore.Muon;
import io.muoncore.spring.annotations.EnableMuon;
import io.muoncore.spring.annotations.MuonController;
import io.muoncore.transport.TransportMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.rx.broadcast.Broadcaster;

import javax.annotation.PostConstruct;

// tag::autowire[]
@SpringBootApplication
@EnableMuon(serviceName = "myService")
@MuonController
public class AutowireMuon {

    @Autowired Muon muon;

    @PostConstruct
    public void tapTransport() {
        Broadcaster<TransportMessage> broadcaster = Broadcaster.create();
        broadcaster.consume( msg -> {
            System.out.println("Message seen from " + msg.getSourceServiceName());
        });
        muon.getTransportControl().tap( msg -> true).subscribe(broadcaster);
    }

    public static void main(String[] args) {
        SpringApplication.run(AutowireMuon.class, args);
    }
}
// end::autowire[]