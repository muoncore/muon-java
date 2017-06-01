package io.muoncore.extension.amqp.externalbroker
import io.muoncore.Discovery
import io.muoncore.InstanceDescriptor
import io.muoncore.Muon
import io.muoncore.ServiceDescriptor
import io.muoncore.MultiTransportMuon
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.extension.amqp.AMQPMuonTransport
import io.muoncore.extension.amqp.BaseEmbeddedBrokerSpec
import io.muoncore.extension.amqp.DefaultAmqpChannelFactory
import io.muoncore.extension.amqp.DefaultServiceQueue
import io.muoncore.extension.amqp.MyTestClass
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory
import reactor.Environment

import java.util.concurrent.TimeUnit

import static io.muoncore.codec.types.MuonCodecTypes.listOf

class RequestResponseWorksSpec extends BaseEmbeddedBrokerSpec {

    def discovery = Mock(Discovery) {
        findService(_) >> Optional.of(new ServiceDescriptor("tombola", [], ["application/json+AES"], [], [
                new InstanceDescriptor("123", "tombola", [], [], [new URI("amqp://muon:microservices@localhost")] , [])
        ]))
        getCodecsForService(_) >> { ["application/json"] as String[]}
    }

    def "high level request response protocol works"() {

        Environment.initializeIfEmpty()

        def svc1 = createMuon("simples")
        def svc2 = createMuon("tombolana")

        svc2.handleRequest(all()) {
            it.ok([hi:"there"])
        }

        sleep(5000)

        when:
        def response = svc1.request("request://tombolana/hello", [hello:"world"]).get(1000, TimeUnit.MILLISECONDS)

        then:
        response != null
        response.status == 200
        response.getPayload(Map).hi == "there"

        cleanup:
        svc1.shutdown()
        svc2.shutdown()
    }

    def "request response protocol supports lists"() {
        def svc1 = createMuon("simples")
        def svc2 = createMuon("tombolana")

        List<MyTestClass> expectedList = [
                new MyTestClass(someValue: "v1", someOtherValue: 1),
                new MyTestClass(someValue: "v2", someOtherValue: 2)
        ]

        List<MyTestClass> returnList = [
                new MyTestClass(someValue: "v3", someOtherValue: 3),
                new MyTestClass(someValue: "v4", someOtherValue: 4)
        ]

        List<MyTestClass> serviceReceivedList = null

        svc2.handleRequest(all()) {
            serviceReceivedList = it.request.getPayload(listOf(MyTestClass))
            assert it.request.getPayload(listOf(MyTestClass)) == expectedList

            it.ok(returnList)
        }

        sleep(5000)

        when:
        def response = svc1.request("request://tombolana/hello", expectedList).get(1000, TimeUnit.MILLISECONDS)

        then:
        serviceReceivedList == expectedList
        response != null
        response.status == 200
        response.getPayload(listOf(MyTestClass)) == returnList

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

        svc1.handleRequest(all()) {
            times << System.currentTimeMillis()
            it.request.getPayload(Map).id
            Thread.sleep(4000)
            it.ok([hi:"there"])
            println "Responded"
        }

        sleep(5000)

        when:
        def responses = []
        responses << svc2.request("request://simples/hello", [hello:"world"])
        sleep(100)
        10.times {
            responses << svc3.request("request://simples/hello", [hello: "world"])
        }
        responses*.get()
        println "Responses retrieved!"

        then: "The timings indicate concurrent access"

        def max = Collections.max(times)
        def min = Collections.min(times)

        def difference = Math.abs(max - min)
        difference < 1000

        cleanup:
        svc1.shutdown()
        svc2.shutdown()
        svc3.shutdown()
    }

    def "service A can be called by B and C concurrently in a non blocking way"() {

        given: "A RPC handler that takes a long time to complete"
        Environment.initializeIfEmpty()

        def svc1 = createMuon("simples")
        def svc2 = createMuon("tombolana")
        def svc3 = createMuon("tombola")

        def times = Collections.synchronizedList([])
        def requests = Collections.synchronizedList([])

        svc1.handleRequest(all()) {
            times << System.currentTimeMillis()
            requests << it
        }

        sleep(5000)

        when:
        def responses = []
        responses << svc2.request("request://simples/hello", [hello:"world"])
        sleep(100)
        200.times {
            responses << svc3.request("request://simples/hello", [hello: "world"])
        }

        Thread.sleep(2000)

        //release all ok
        requests*.ok([hi:"there"])

        responses*.get()

        then: "The timings indicate concurrent access"

        def max = Collections.max(times)
        def min = Collections.min(times)

        def difference = Math.abs(max - min)
        difference < 1500

        cleanup:
        svc1.shutdown()
        svc2.shutdown()
        svc3.shutdown()
    }

    private Muon createMuon(serviceName) {

        def connection = new RabbitMq09ClientAmqpConnection("amqp://muon:microservices@localhost")
        def queueFactory = new RabbitMq09QueueListenerFactory(connection.channel)
        def serviceQueue = new DefaultServiceQueue(serviceName, connection)
        def channelFactory = new DefaultAmqpChannelFactory(serviceName, queueFactory, connection)

        def svc1 = new AMQPMuonTransport(
                "amqp://muon:microservices@localhost", serviceQueue, channelFactory)

        def config = new AutoConfiguration(serviceName:serviceName)
        def muon = new MultiTransportMuon(config, discovery, [svc1], new JsonOnlyCodecs())

        muon
    }

}
