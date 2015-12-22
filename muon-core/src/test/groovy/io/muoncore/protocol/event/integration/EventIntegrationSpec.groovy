package io.muoncore.protocol.event.integration
import com.google.common.eventbus.EventBus
import io.muoncore.Muon
import io.muoncore.SingleTransportMuon
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.protocol.event.Event
import io.muoncore.protocol.event.server.EventServerProtocolStack
import reactor.Environment
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class EventIntegrationSpec extends Specification {

    def discovery = new InMemDiscovery()
    def eventbus = new EventBus()

    def "can emit a series of events and have them recieved on the server side"() {

        Environment.initializeIfEmpty()

        def data = []

        def muon1 = muon("simples")
        def muon2 = muonEventStore { Event ev ->
            println "Event is the awesome ${ev}"
            data << ev
        }

        when:

        muon1.event(new Event("SomethingHappened", "myid", "none", "muon1", "HELLO WORLD"))
        muon1.event(new Event("SomethingHappened", "myid", "none", "muon1", "HELLO WORLD"))
        muon1.event(new Event("SomethingHappened", "myid", "none", "muon1", "HELLO WORLD"))
        muon1.event(new Event("SomethingHappened", "myid", "none", "muon1", "HELLO WORLD"))

        then:
        new PollingConditions(timeout: 20).eventually {
            data.size() == 4

        }
    }

    def "data remains in order"() {

        def data = []

        def muon1 = muon("simples")
        def muon2 = muonEventStore { Event ev ->
            println "Event is the awesome ${ev}"
            data << ev
        }

        when:
        200.times {
            muon1.event(new Event("SomethingHappened", "${it}", "none", "muon1", "HELLO WORLD"))
        }
        sleep(5000)

        def sorted = new ArrayList<Event>(data).sort {
            Integer.parseInt(it.id)
        }

        then:

        data == sorted
    }

    Muon muon(name) {
        def config = new AutoConfiguration(serviceName: name, aesEncryptionKey: "abcde12345678906")
        def transport = new InMemTransport(config, eventbus)

        new SingleTransportMuon(config, discovery, transport)
    }
    public Muon muonEventStore(Closure handler) {
        def config = new AutoConfiguration(tags:["eventstore"], serviceName: "chronos", aesEncryptionKey: "abcde12345678906")
        def transport = new InMemTransport(config, eventbus)

        def muon = new SingleTransportMuon(config, discovery, transport)

        muon.protocolStacks.registerServerProtocol(new EventServerProtocolStack(handler, muon.codecs))

        muon
    }
}
