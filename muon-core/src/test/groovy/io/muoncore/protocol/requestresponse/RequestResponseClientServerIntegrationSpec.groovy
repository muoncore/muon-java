package io.muoncore.protocol.requestresponse

import io.muoncore.channel.Channels
import io.muoncore.channel.async.StandardAsyncChannel
import io.muoncore.codec.Codecs
import io.muoncore.codec.JsonOnlyCodecs
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocolStack
import io.muoncore.protocol.requestresponse.server.DynamicRequestResponseHandlers
import io.muoncore.protocol.requestresponse.server.RequestResponseHandlers
import io.muoncore.protocol.requestresponse.server.RequestResponseServerHandler
import io.muoncore.protocol.requestresponse.server.RequestResponseServerProtocolStack
import io.muoncore.protocol.requestresponse.server.RequestWrapper
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.client.TransportClient
import spock.lang.Specification
import spock.lang.Timeout

import java.util.function.Predicate

class RequestResponseClientServerIntegrationSpec extends Specification {

    @Timeout(2)
    def "client and server can communicate"() {
        given:

        def handlers = new DynamicRequestResponseHandlers(new RequestResponseServerHandler() {
            @Override
            Predicate<Request> getPredicate() {
                return { false } as Predicate
            }

            @Override
            void handle(RequestWrapper request) {
                request.answer(new Response("from default", "from default"))
            }
        })
        handlers.addHandler(new RequestResponseServerHandler() {
            @Override
            Predicate<Request> getPredicate() {
                return {
                    it.id == "1234"
                }
            }

            @Override
            void handle(RequestWrapper request) {
                request.answer(new Response("hello", "/hello"))
            }
        })

        def server = new RequestResponseServerProtocolStack(handlers, new JsonOnlyCodecs())

        def channel = new StandardAsyncChannel()

        //does the conversion from outbound<=>inbound that the transport pair would ordinarily do.
        Channels.connectAndTransform(
                server.createChannel(),
                channel.left(),
                {
                    new TransportInboundMessage(it.id, it.serviceName, it.protocol)
                },
                {
                    new TransportInboundMessage(it.id, it.serviceName, it.protocol)
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
        }

        when:
        Response response = client.request(new Request(id: "1234")).get()

        then:
        response.id == "hello"
    }
}
