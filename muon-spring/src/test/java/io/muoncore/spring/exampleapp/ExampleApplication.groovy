package io.muoncore.spring.exampleapp

import io.muoncore.protocol.event.Event
import io.muoncore.spring.annotations.EventSourceListener
import io.muoncore.spring.annotations.MuonController
import io.muoncore.spring.repository.MuonEventStoreRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import reactor.rx.broadcast.Broadcaster

@SpringBootApplication
@MuonController
@EnableScheduling
class ExampleApplication {

    @Autowired Broadcaster broadcaster;
    @Autowired MuonEventStoreRepository eventStoreRepository;

    private List<String> values = new ArrayList<>()

    /**
     * Listen to an event source
     */
    @EventSourceListener(String)
    public void ingestData(String value) {
        values << value
    }

    /**
     * Listen to an event source
     */
    @EventSourceListener(String)
    public void ingestData(Event value) {
        println "Received event ${value.id}"
    }

    /**
     * Emits events every so often.
     * These go onto the default 'general' stream
     */
    @Scheduled(fixedRate = 5000l)
    public void sendMessage() {
        eventStoreRepository.event("Hello World");
    }

    static void main(def args) {
        SpringApplication.run(ExampleApplication)
    }
}


//@PostConstruct
//void post(SingleTransportMuon muon) {
//    muon.protocolStacks.registerServerProtocol(new EventServerProtocolStack({
//
//
//
//    }, muon.codecs))
//}

//    add a @MuonStreamListener(reconnectMode=true) Subscriber<X> myMethod()
//      - if return subscriber, use that instead of generating one.
//    add above annotation. just does a lookup of the event store service name by tag and does a stream against that.
//    add a core muon api to do this.
//    add a @MuonStreamPublisher that returns ether a Publisher or a ReactiveStreamServerHandlerApi.PublisherGenerator
