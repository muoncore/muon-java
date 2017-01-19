package io.muoncore.spring.e2e.boot
import io.muoncore.Muon
import io.muoncore.spring.annotations.EnableMuon
import io.muoncore.spring.annotations.EventSourceListener
import io.muoncore.spring.annotations.MuonController
import io.muoncore.spring.repository.MuonEventStoreRepository
import io.muoncore.spring.teststore.EventStoreInMem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class MultiBootSpec extends Specification {

    def "run several boot/muon services in parallel and have them communicate"() {

        given: "An event store and an event emitting service"
        def testStore = SpringApplication.run(EventStoreInMem).getBean(EventStoreInMem)
        def svc1 = SpringApplication.run(BootService1).getBean(BootService1)

        when: "A new service is started later on that requires the historical events"
        sleep(2000)
        def svc2 = SpringApplication.run(BootService2).getBean(BootService2)

        then: "historical events have been replayed into the service"
        new PollingConditions().eventually {
            svc2.dataList.size() > svc1.issued
        }
    }
}

@SpringBootApplication
@MuonController
@EnableMuon(serviceName = "service1")
class BootService2 {

    List dataList = []

    @Autowired Muon muon

    @EventSourceListener()
    def receiveData(Map data) {
        println "Event is being checked ${data}"
        dataList << data
    }
}

@SpringBootApplication
@MuonController
@EnableScheduling
@EnableMuon(serviceName = "service2")
class BootService1 {

    @Autowired Muon muon
    @Autowired MuonEventStoreRepository eventStore

    int issued = 0

    @Scheduled(fixedRate = 5l)
    void emitEvent() {
        println "Emitting!"
        issued++
//        eventStore.event("SomethingHappened", [message:"awesome"])
    }
}
