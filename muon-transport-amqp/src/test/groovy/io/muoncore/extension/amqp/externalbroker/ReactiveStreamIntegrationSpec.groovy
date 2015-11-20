package io.muoncore.extension.amqp.externalbroker
import com.google.common.eventbus.EventBus
import io.muoncore.Muon
import io.muoncore.SingleTransportMuon
import io.muoncore.config.AutoConfiguration
import io.muoncore.extension.amqp.AMQPMuonTransport
import io.muoncore.extension.amqp.DefaultAmqpChannelFactory
import io.muoncore.extension.amqp.DefaultServiceQueue
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory
import io.muoncore.memory.discovery.InMemDiscovery
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
        20.times {
            b.accept(["hello": "world"])
        }

        then:
        new PollingConditions(timeout: 5).eventually {
            data.size() == 20
        }

        cleanup:
        muon1.shutdown()
        muon2.shutdown()
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

    private Muon muon(serviceName) {

        def connection = new RabbitMq09ClientAmqpConnection("amqp://muon:microservices@localhost")
        def queueFactory = new RabbitMq09QueueListenerFactory(connection.channel)
        def serviceQueue = new DefaultServiceQueue(serviceName, connection)
        def channelFactory = new DefaultAmqpChannelFactory(serviceName, queueFactory, connection)

        def svc1 = new AMQPMuonTransport(
                "amqp://muon:microservices@localhost", serviceQueue, channelFactory)

        def config = new AutoConfiguration(serviceName:serviceName, aesEncryptionKey: "abcde12345678906")
        def muon = new SingleTransportMuon(config, discovery, svc1)

        muon
    }
}
