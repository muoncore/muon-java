package io.muoncore.protocol.reactivestream.integration
import com.google.common.eventbus.EventBus
import io.muoncore.Muon
import io.muoncore.SingleTransportMuon
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.protocol.reactivestream.server.PublisherLookup
import reactor.Environment
import reactor.rx.broadcast.Broadcaster
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class ReactiveStreamIntegrationSpec extends Specification {

    def discovery = new InMemDiscovery()
    def eventbus = new EventBus()

    def "can create a publisher and subscribe to it remotely"() {

        Environment.initializeIfEmpty()

        def data = []

        def b = Broadcaster.create()
        def sub2 = Broadcaster.create()

        sub2.consume {
            data << it
        }

        def muon1 = muon("simples")
        def muon2 = muon("tombola")

        muon1.publishSource("somedata", PublisherLookup.PublisherType.HOT, b)

        when:
        muon2.subscribe(new URI("stream://simples/somedata"), Map, sub2)

        and:
        50000.times {
            b.accept(["hello": "world"])
        }

        then:
        new PollingConditions().eventually {
            data.size() == 50000
        }
    }

    @Ignore
    def "subscribing to remote fails with onError"() {

        def data = []
        def errorReceived = false

        Environment env = Environment.initialize()

        def sub2 = Broadcaster.create(env)
        sub2.observeError(Exception, {
            println "ERROR WAS FOUND"
            errorReceived = true
        }).consume {
            println "BAD JUJU"
        }

        sub2.consume {
            println "SOmething good?"
            data << it
        }

        def muon1 = muon("simples")
        def muon2 = muon("tombola")

        when:
        muon2.subscribe(new URI("stream://simples/BADSTREAM"), Map, sub2)

        then:
        new PollingConditions().eventually {
            errorReceived
        }
    }

    Muon muon(name) {
        def config = new AutoConfiguration(serviceName: name, aesEncryptionKey: "abcde12345678906")
        def transport = new InMemTransport(config, eventbus)

        new SingleTransportMuon(config, discovery, transport)
    }
}
