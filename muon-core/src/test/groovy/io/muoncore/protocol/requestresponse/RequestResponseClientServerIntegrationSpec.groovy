package io.muoncore.protocol.requestresponse
import io.muoncore.channel.Channels
import io.muoncore.channel.async.StandardAsyncChannel
import io.muoncore.codec.Codecs
import io.muoncore.codec.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocolStack
import io.muoncore.protocol.requestresponse.server.DynamicRequestResponseHandlers
import io.muoncore.protocol.requestresponse.server.RequestResponseServerHandler
import io.muoncore.protocol.requestresponse.server.RequestResponseServerProtocolStack
import io.muoncore.protocol.requestresponse.server.RequestWrapper
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportOutboundMessage
import io.muoncore.transport.client.TransportClient
import spock.lang.Specification
import spock.lang.Timeout

import java.util.function.Predicate

class RequestResponseClientServerIntegrationSpec extends Specification {

//    @Timeout(2)
    def "client and server can communicate"() {
        given:

        def handlers = new DynamicRequestResponseHandlers(new RequestResponseServerHandler() {
            @Override
            Predicate<RequestMetaData> getPredicate() {
                return { false } as Predicate
            }

            @Override
            void handle(RequestWrapper request) {
                request.answer(new Response(200, [message:"defaultservice"]))
            }

            @Override
            Class getRequestType() {
                return Object
            }
        })
        handlers.addHandler(new RequestResponseServerHandler() {
            @Override
            Predicate<RequestMetaData> getPredicate() {
                return {
                    it.sourceService == "tombola"
                }
            }

            @Override
            void handle(RequestWrapper request) {
                request.answer(new Response(200, [message:"hello"]))
            }

            @Override
            Class getRequestType() {
                return Object
            }
        })

        def server = new RequestResponseServerProtocolStack(handlers, new JsonOnlyCodecs())

        def channel = new StandardAsyncChannel()

        //does the conversion from outbound<=>inbound that the transport pair would ordinarily do.
        Channels.connectAndTransform(
                server.createChannel(),
                channel.left(),
                { TransportOutboundMessage msg ->
                    new TransportInboundMessage(
                            msg.id,
                            msg.sourceServiceName,
                            msg.protocol,
                            msg.metadata,
                            msg.contentType,
                            msg.payload)
                },
                { TransportOutboundMessage msg ->
                    new TransportInboundMessage(msg.id,
                            msg.sourceServiceName,
                            msg.protocol,
                            msg.metadata,
                            msg.contentType,
                            msg.payload)
                }
        )

        def transportClient = Mock(TransportClient) {
            openClientChannel() >> channel.right()
        }

        def client = new RequestResponseClientProtocolStack() {
            @Override
            TransportClient getTransportClient() {
                return transportClient
            }

            @Override
            Codecs getCodecs() {
                return new JsonOnlyCodecs()
            }

            @Override
            AutoConfiguration getConfiguration() {
                return new AutoConfiguration(serviceName: "tombola")
            }
        }

        when:
        Response response = client.request(
                new Request(new RequestMetaData("myapp", "someservice"), [message:"yoyo"]), Map).get()

        then:
        response.status == 200
        response.payload?.message == "hello"
    }
}
