package io.muoncore.extension.amqp.externalbroker

import io.muoncore.Discovery
import io.muoncore.Muon
import io.muoncore.SingleTransportMuon
import io.muoncore.codec.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.extension.amqp.AMQPMuonTransport
import io.muoncore.extension.amqp.DefaultAmqpChannelFactory
import io.muoncore.extension.amqp.DefaultServiceQueue
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory
import io.muoncore.protocol.DynamicRegistrationServerStacks
import io.muoncore.protocol.defaultproto.DefaultServerProtocol
import io.muoncore.protocol.requestresponse.RRPTransformers
import io.muoncore.protocol.requestresponse.Request
import io.muoncore.protocol.requestresponse.RequestMetaData
import io.muoncore.protocol.requestresponse.Response
import io.muoncore.protocol.requestresponse.server.DynamicRequestResponseHandlers
import io.muoncore.protocol.requestresponse.server.RequestResponseServerHandler
import io.muoncore.protocol.requestresponse.server.RequestResponseServerProtocolStack
import io.muoncore.protocol.requestresponse.server.RequestWrapper
import spock.lang.Specification

import java.util.concurrent.TimeUnit
import java.util.function.Predicate

class RequestResponseWorksSpec extends Specification {

    def discovery = Mock(Discovery)

    def "high level request response protocol works"() {

        def svc1 = createMuon("simples")
        def svc2 = createMuon("tombola")

        svc2.handleRequest({ true }, {
            it.request.id
            it.answer(new Response(200, [hi:"there"]))
        }, Map)

        when:
        def response = svc1.request(new Request(new RequestMetaData("hello","tombola"), [hello:"world"]), Map).get(1000, TimeUnit.MILLISECONDS)

        then:
        response != null
        response.status == 200
        response.payload.hi == "there"
    }

    private Muon createMuon(serviceName) {

        def connection = new RabbitMq09ClientAmqpConnection("amqp://muon:microservices@localhost")
        def queueFactory = new RabbitMq09QueueListenerFactory(connection.channel)
        def serviceQueue = new DefaultServiceQueue(serviceName, connection)
        def channelFactory = new DefaultAmqpChannelFactory(serviceName, queueFactory, connection)
        def serverStacks = new DynamicRegistrationServerStacks(new DefaultServerProtocol())

        //add in the request response server protocol
        serverStacks.registerServerProtocol(RRPTransformers.REQUEST_RESPONSE_PROTOCOL, new RequestResponseServerProtocolStack(
                new DynamicRequestResponseHandlers(new RequestResponseServerHandler() {
                    @Override
                    Predicate<RequestMetaData> getPredicate() {
                        return { false }
                    }

                    @Override
                    void handle(RequestWrapper request) {
                        request.answer(new Response(404, [:]))
                    }

                    @Override
                    Class getRequestType() {
                        return Map
                    }
                }), new JsonOnlyCodecs()
        ))

        def svc1 = new AMQPMuonTransport(
                "amqp://muon:microservices@localhost", serviceName, serverStacks, serviceQueue, channelFactory)

        svc1.start()

        def config = new AutoConfiguration(serviceName:serviceName)
        def muon = new SingleTransportMuon(config, discovery, svc1)

        muon
    }
}
