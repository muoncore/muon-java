package io.muoncore.protocol.requestresponse

import io.muoncore.codec.GsonCodec
import io.muoncore.codec.JsonOnlyCodecs
import io.muoncore.protocol.requestresponse.server.RequestResponseHandlers
import io.muoncore.protocol.requestresponse.server.RequestResponseServerHandler
import io.muoncore.protocol.requestresponse.server.RequestResponseServerHandlerApi
import io.muoncore.protocol.requestresponse.server.RequestResponseServerProtocolStack
import io.muoncore.protocol.requestresponse.server.RequestWrapper
import io.muoncore.transport.TransportInboundMessage
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class RequestResponseServerProtocolStackSpec extends Specification {

    def "createChannel gives a channel that calls findHandler on a message received"() {
        def handlers = Mock(RequestResponseHandlers) {
            findHandler(_) >> Mock(RequestResponseServerHandlerApi.Handler)
        }
        def stack = new RequestResponseServerProtocolStack(handlers, new JsonOnlyCodecs())

        when:
        def channel = stack.createChannel()
        channel.send(inbound("123", "FAKESERVICE", "requestresponse"))
        Thread.sleep(50)

        then:
        1 * handlers.findHandler(_)
    }

    def "handler can be invoked via the external channel"() {

        def handler = Mock(RequestResponseServerHandler) {
            getRequestType() >> Map
        }
        def handlers = Mock(RequestResponseHandlers) {
            findHandler(_) >> handler

        }
        def stack = new RequestResponseServerProtocolStack(handlers, new JsonOnlyCodecs())

        when:
        def channel = stack.createChannel()
        channel.send(inbound("123", "FAKESERVICE", "requestresponse"))
        Thread.sleep(50)

        then:
        1 * handler.handle(_)
    }
    def "handler can reply down the channel"() {

        def handler = Mock(RequestResponseServerHandler) {
            handle(_) >> { RequestWrapper wrapper ->
                wrapper.answer(new Response(200, "hello"))
            }
            getRequestType() >> Map
        }

        def handlers = Mock(RequestResponseHandlers) {
            findHandler(_) >> handler
        }
        def stack = new RequestResponseServerProtocolStack(handlers, new JsonOnlyCodecs())

        def responseReceived

        when:
        def channel = stack.createChannel()
        channel.receive({
            responseReceived = it
        })

        channel.send(inbound("123", "FAKESERVICE", "requestresponse"))
        Thread.sleep(50)

        then:
        new PollingConditions().eventually {
            responseReceived != null
        }
    }

    def inbound(id, service, protocol) {
        new TransportInboundMessage(
                id,
                service,
                protocol,
                [:],
                "application/json",
                new GsonCodec().encode([:]))
    }
}


