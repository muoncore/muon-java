package io.muoncore.spring.exampleapp
import io.muoncore.Muon
import io.muoncore.protocol.reactivestream.server.PublisherLookup
import io.muoncore.spring.annotations.MuonController
import io.muoncore.spring.annotations.MuonStreamListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import reactor.Environment
import reactor.rx.broadcast.Broadcaster

@SpringBootApplication
@MuonController
@EnableScheduling
class ExampleApplication {

    @Autowired Broadcaster broadcaster;

    @MuonStreamListener(url = "stream://simpleService/myAwesome2")
    public String something(String value) {
        System.out.println("Received data! " + value);
    }

    @Bean public Broadcaster configStuff(Muon muon) {
        Broadcaster pub = Broadcaster.create(Environment.initializeIfEmpty())
        muon.publishSource("myAwesome", PublisherLookup.PublisherType.HOT, pub)
        return pub;
    }

    @Scheduled(fixedRate = 5000l)
    public void sendMessage() {

        broadcaster.accept("Hello World");
    }

    static void main(def args) {
        SpringApplication.run(ExampleApplication)
    }
}
