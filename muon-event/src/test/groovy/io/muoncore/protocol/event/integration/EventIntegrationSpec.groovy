package io.muoncore.protocol.event.integration

import com.google.common.eventbus.EventBus
import io.muoncore.MultiTransportMuon
import io.muoncore.Muon
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.protocol.event.ClientEvent
import io.muoncore.protocol.event.Event
import io.muoncore.protocol.event.client.DefaultEventClient
import io.muoncore.protocol.event.client.EventResult
import io.muoncore.protocol.event.server.EventServerProtocolStack
import io.muoncore.protocol.event.server.EventWrapper
import reactor.Environment
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class EventIntegrationSpec extends Specification {

    def discovery = new InMemDiscovery()
    def eventbus = new EventBus()

    def "can emit a series of events and have them recieved on the server side"() {

        Environment.initializeIfEmpty()

        def data = []
        List<EventResult> results = []

        boolean fail = true

        def muon2 = muonEventStore { EventWrapper ev ->
            println "Event is the awesome ${ev.event}"
            data << ev.event
            if (!fail) {
                ev.persisted("ORDERID", System.currentTimeMillis())
                fail = true
            } else {
                ev.failed("Something went wrong")
                fail = false
            }
        }

        def muon1 = muon("simples")

        def evClient = new DefaultEventClient(muon1)


        when:

        results << evClient.event(new ClientEvent("awesome", "SomethingHappened", "myid", "none", "muon1", "HELLO WORLD"))
        results << evClient.event(new ClientEvent("awesome", "SomethingHappened", "myid", "none", "muon1", "HELLO WORLD"))
        results << evClient.event(new ClientEvent("awesome", "SomethingHappened", "myid", "none", "muon1", "HELLO WORLD"))
        results << evClient.event(new ClientEvent("awesome", "SomethingHappened", "myid", "none", "muon1", "HELLO WORLD"))

        then:
        new PollingConditions().eventually {
            data.size() == 4
            results.size() == 4
            results.findAll { it.status == EventResult.EventResultStatus.PERSISTED }.size() == 2
            results.findAll { it.status == EventResult.EventResultStatus.FAILED }.size() == 2
        }
    }

    def "data remains in order"() {

        def data = []

        def muon2 = muonEventStore { EventWrapper ev ->
            println "Event is the awesome ${ev.event}"
            data << ev.event
            ev.persisted("ORDERID", System.currentTimeMillis())
        }

        def muon1 = muon("simples")
        def evClient = new DefaultEventClient(muon1)

        when:
        200.times {
            evClient.event(new ClientEvent("${it}", "SomethingHappened", "1.0", "none", "muon1", "HELLO WORLD"))
        }

        then:
        new PollingConditions(timeout: 30).eventually {
            data.size() == 200
            def sorted = new ArrayList<Event>(data).sort {
                Integer.parseInt(it.eventType)
            }
            data == sorted
        }
    }

    Muon muon(name) {
        def config = new AutoConfiguration(serviceName: name)
        def transport = new InMemTransport(config, eventbus)

        new MultiTransportMuon(config, discovery, [transport])
    }
    public Muon muonEventStore(Closure handler) {
        def config = new AutoConfiguration(tags:["eventstore"], serviceName: "chronos")
        def transport = new InMemTransport(config, eventbus)

        def muon = new MultiTransportMuon(config, discovery, [transport])

        muon.protocolStacks.registerServerProtocol(new EventServerProtocolStack(handler, muon.codecs))

        muon
    }
}
