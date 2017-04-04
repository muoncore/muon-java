package io.muoncore.examples

import com.google.common.eventbus.EventBus
import io.muoncore.Muon
import io.muoncore.MuonBuilder
import io.muoncore.config.MuonConfigBuilder
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.protocol.event.ClientEvent
import io.muoncore.protocol.event.Event
import io.muoncore.protocol.event.client.DefaultEventClient
import io.muoncore.protocol.event.client.EventResult
import io.muoncore.protocol.event.server.EventServerProtocolStack
import io.muoncore.protocol.event.server.EventWrapper
import reactor.Environment
import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@IgnoreIf({ System.getenv("SHORT_TEST") })
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
                ev.persisted(5555, System.currentTimeMillis())
                fail = true
            } else {
                ev.failed("Something went wrong")
                fail = false
            }
        }

        def muon1 = muon("simples")

        def evClient = new DefaultEventClient(muon1)


        when:

        results << evClient.event(new ClientEvent("awesome", "SomethingHappened", "1.0", 1234, "none", [msg:"HELLO WORLD"]))
        results << evClient.event(new ClientEvent("awesome", "SomethingHappened", "1.0", 1234, "none", [msg:"HELLO WORLD"]))
        results << evClient.event(new ClientEvent("awesome", "SomethingHappened", "1.0", 1234, "none", [msg:"HELLO WORLD"]))
        results << evClient.event(new ClientEvent("awesome", "SomethingHappened", "1.0", 1234, "none", [msg:"HELLO WORLD"]))

        then:
        new PollingConditions().eventually {
            data.size() == 4
            results.size() == 4
            results.findAll { it.status == EventResult.EventResultStatus.PERSISTED }.size() == 2
            results.findAll { it.status == EventResult.EventResultStatus.FAILED }.size() == 2
        }
        results[1].eventTime > 0
        results[1].orderId == 5555

        cleanup:
        muon1.shutdown()
        muon2.shutdown()
    }

    def "data remains in client order"() {

        def data = Collections.synchronizedList([])

        def muon2 = muonEventStore { EventWrapper ev ->
            println "Event is the awesome ${ev.event}"
            data << ev.event
            ev.persisted(1234, System.currentTimeMillis())
        }

        def muon1 = muon("simples")
        def evClient = new DefaultEventClient(muon1)

        when:
        200.times {
            evClient.event(new ClientEvent(
                    "${it}", "SomethingHappened", "1.0", 432, "none", [msg:"HELLO WORLD"]))
        }

        then:
        new PollingConditions(timeout: 5).eventually {
            data.size() == 200
            def sorted = new ArrayList<Event>(data).sort {
                Integer.parseInt(it.eventType)
            }
            data == sorted
        }

        cleanup:
        muon1?.shutdown()
        muon2?.shutdown()
    }

    def "many clients can submit events"() {

        Environment.initializeIfEmpty()

        def data = Collections.synchronizedList([])
        List<EventResult> results = Collections.synchronizedList([])

        boolean fail = true

        def muon2 = muonEventStore { EventWrapper ev ->
            println "Event is the awesome ${ev.event}"
            data << ev.event
            if (!fail) {
                ev.persisted(1234, System.currentTimeMillis())
                fail = true
            } else {
                ev.failed("Something went wrong")
                fail = false
            }
        }

        def muons = []
        def parallelClients = 2
        def messagesPerClient = 100

        parallelClients.times {
            muons << muon("simples${it}")
        }

        def clients = muons.collect { new DefaultEventClient(it) }

        when:

        clients.each { client ->
            Thread.start {
                messagesPerClient.times {
                    results << client.event(new ClientEvent("awesome", "SomethingHappened", "1.0",  4231, "none", [msg:"HELLO WORLD"]))
                }
            }
        }

        then:
        new PollingConditions(timeout: 20).eventually {
            data.size() == parallelClients * messagesPerClient
            results.size() == parallelClients * messagesPerClient
            results.findAll { it.status == EventResult.EventResultStatus.PERSISTED }.size() == parallelClients * messagesPerClient / 2
            results.findAll { it.status == EventResult.EventResultStatus.FAILED }.size() == parallelClients * messagesPerClient / 2
        }

        cleanup:
        muons*.shutdown()
        muon2.shutdown()
    }

    def "many event submissions can stack up"() {

        Environment.initializeIfEmpty()

        def data = Collections.synchronizedList([])
        List<EventResult> results = Collections.synchronizedList([])

        boolean fail = true

        def muon2 = muonEventStore { EventWrapper ev ->
            println "Event is the awesome ${ev.event}"
            data << ev.event
            if (!fail) {
                ev.persisted(13, 55555)
                fail = true
            } else {
                ev.failed("Something went wrong")
                fail = false
            }
        }

        def muons = []
        def parallelClients = 10
        def messagesPerClient = 100

        parallelClients.times {
            muons << muon("simples${it}")
        }

        def clients = muons.collect { new DefaultEventClient(it) }

        when:

        clients.each { client ->
            Thread.start {
                messagesPerClient.times {
                    results << client.event(
                            new ClientEvent("awesome", "SomethingHappened", "1.0",  141321, "none", [msg:"HELLO WORLD"]))
                }
            }
        }

        then:
        new PollingConditions(timeout: 20).eventually {
            data.size() == parallelClients * messagesPerClient
            results.size() == parallelClients * messagesPerClient
            results.findAll { it.status == EventResult.EventResultStatus.PERSISTED }.size() == parallelClients * messagesPerClient / 2
            results.findAll { it.status == EventResult.EventResultStatus.FAILED }.size() == parallelClients * messagesPerClient / 2
        }

        cleanup:
        muons*.shutdown()
        muon2.shutdown()
    }

    Muon muon(name) {
        def config = MuonConfigBuilder.withServiceIdentifier(name)
                .build()

        MuonBuilder.withConfig(config).build()
    }
    public Muon muonEventStore(Closure handler) {


        def config = MuonConfigBuilder.withServiceIdentifier("chronos")
                .withTags("eventstore")
                .build()

        def muon = MuonBuilder.withConfig(config).build()

        muon.protocolStacks.registerServerProtocol(new EventServerProtocolStack(handler, muon.codecs, muon.discovery))

        muon
    }
}

/*

{"schema":"1.0","service-id":"simples","stream-name":"SomethingHappened","payload":"HELLO WORLD","caused-by":1234,"event-type":"awesome","caused-by-relation":"none"}


 */
