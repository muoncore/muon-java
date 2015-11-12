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

        def svc1 = createMuon("simples")
        def svc2 = createMuon("tombola")

        svc2.handleRequest(all(), Map) {
            it.request.id
            it.ok([hi:"there"])
        }

        when:
        def response = svc1.request("request://tombola/hello", [hello:"world"], Map).get(500, TimeUnit.MILLISECONDS)

        then:
        response != null
        response.status == 200
        response.payload.hi == "there"

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
