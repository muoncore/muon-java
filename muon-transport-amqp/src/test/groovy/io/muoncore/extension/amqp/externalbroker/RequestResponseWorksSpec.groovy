package io.muoncore.extension.amqp.externalbroker
import io.muoncore.Discovery
import io.muoncore.Muon
import io.muoncore.ServiceDescriptor
import io.muoncore.SingleTransportMuon
import io.muoncore.config.AutoConfiguration
import io.muoncore.extension.amqp.AMQPMuonTransport
import io.muoncore.extension.amqp.DefaultAmqpChannelFactory
import io.muoncore.extension.amqp.DefaultServiceQueue
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory
import reactor.Environment
import spock.lang.IgnoreIf
import spock.lang.Specification

import java.util.concurrent.TimeUnit

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all

@IgnoreIf({ System.getenv("BUILD_NUMBER") })
class RequestResponseWorksSpec extends Specification {

    def discovery = Mock(Discovery) {
        findService(_) >> Optional.of(new ServiceDescriptor("tombola", [], ["application/json+AES"], []))
    }

    def "high level request response protocol works"() {

        Environment.initializeIfEmpty()

        def svc1 = createMuon("simples")
        def svc2 = createMuon("tombolana")

        svc2.handleRequest(all(), Map) {
            it.request.id
            it.ok([hi:"there"])
        }

        sleep(5000)

        when:
        def response = svc1.request("request://tombolana/hello", [hello:"world"], Map).get(1000, TimeUnit.MILLISECONDS)

        then:
        response != null
        response.status == 200
        response.payload.hi == "there"

        cleanup:
        svc1.shutdown()
        svc2.shutdown()
    }

    def "service A can be called by B and C concurrently"() {

        given: "A RPC handler that takes a long time to complete"
        Environment.initializeIfEmpty()

        def svc1 = createMuon("simples")
        def svc2 = createMuon("tombolana")
        def svc3 = createMuon("tombola")

        def times = Collections.synchronizedList([])

        svc1.handleRequest(all(), Map) {
            times << System.currentTimeMillis()
            it.request.id
            Thread.sleep(4000)
            it.ok([hi:"there"])
        }

        sleep(5000)

        when:
        def responses = []
        responses << svc2.request("request://simples/hello", [hello:"world"], Map)
        sleep(100)
        100.times {
            responses << svc3.request("request://simples/hello", [hello: "world"], Map)
        }
        responses*.get()

        then: "The timings indicate concurrent access"

        def max = Collections.max(times)
        def min = Collections.min(times)

        def difference = Math.abs(max - min)
        difference < 1000

        cleanup:
        svc1.shutdown()
        svc2.shutdown()
    }

    def "service A can be called by B and C concurrently in a non blocking way"() {

        given: "A RPC handler that takes a long time to complete"
        Environment.initializeIfEmpty()

        def svc1 = createMuon("simples")
        def svc2 = createMuon("tombolana")
        def svc3 = createMuon("tombola")

        def times = Collections.synchronizedList([])
        def requests = Collections.synchronizedList([])

        svc1.handleRequest(all(), Map) {
            times << System.currentTimeMillis()
            it.request.id
            requests << it
        }

        sleep(5000)

        when:
        def responses = []
        responses << svc2.request("request://simples/hello", [hello:"world"], Map)
        sleep(100)
        200.times {
            responses << svc3.request("request://simples/hello", [hello: "world"], Map)
        }

        Thread.sleep(2000)

        //release all ok
        requests*.ok([hi:"there"])

        responses*.get()

        then: "The timings indicate concurrent access"

        def max = Collections.max(times)
        def min = Collections.min(times)

        def difference = Math.abs(max - min)
        difference < 1000

        cleanup:
        svc1.shutdown()
        svc2.shutdown()
    }

    private Muon createMuon(serviceName) {

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
