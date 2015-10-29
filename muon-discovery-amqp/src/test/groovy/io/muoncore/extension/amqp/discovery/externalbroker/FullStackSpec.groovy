package io.muoncore.extension.amqp.discovery.externalbroker

import io.muoncore.Discovery
import io.muoncore.Muon
import io.muoncore.SingleTransportMuon
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.extension.amqp.AMQPMuonTransport
import io.muoncore.extension.amqp.DefaultAmqpChannelFactory
import io.muoncore.extension.amqp.DefaultServiceQueue
import io.muoncore.extension.amqp.discovery.AmqpDiscovery
import io.muoncore.extension.amqp.discovery.ServiceCache
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory
import io.muoncore.protocol.requestresponse.Response
import spock.lang.IgnoreIf
import spock.lang.Specification

import java.util.concurrent.TimeUnit

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all

@IgnoreIf({ System.getenv("BUILD_NUMBER") })
class FullStackSpec extends Specification {

    def "full amqp based stack works"() {

        def svc1 = createMuon("simples")
        def svc2 = createMuon("tombola1")
        def svc3 = createMuon("tombola2")
        def svc4 = createMuon("tombola3")
        def svc5 = createMuon("tombola4")
        def svc6 = createMuon("tombola5")

        svc2.handleRequest(all(), Map) {
            it.request.id
            it.answer(new Response(200, [hi:"there"]))
        }

        when:
        Thread.sleep(4500)
        def response = svc1.request("request://tombola1/hello", [hello:"world"], Map).get(500, TimeUnit.MILLISECONDS)
        def discoveredServices = svc3.discovery.knownServices

        then:
        discoveredServices.size() == 6
        response != null
        response.status == 200
        response.payload.hi == "there"

        cleanup:
        svc1.shutdown()
        svc2.shutdown()
        svc3.shutdown()
        svc4.shutdown()
        svc5.shutdown()
        svc6.shutdown()
    }

    private Muon createMuon(serviceName) {

        def discovery = createDiscovery()
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

    private Discovery createDiscovery() {

        def connection = new RabbitMq09ClientAmqpConnection("amqp://muon:microservices@localhost")
        def queueFactory = new RabbitMq09QueueListenerFactory(connection.channel)
        def codecs = new JsonOnlyCodecs()

        def discovery = new AmqpDiscovery(queueFactory, connection, new ServiceCache(), codecs)
        discovery.start()
        discovery
    }
}
