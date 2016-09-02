package io.muoncore.protocol.requestresponse

import io.muoncore.Discovery
import io.muoncore.ServiceDescriptor
import io.muoncore.codec.json.GsonCodec
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.protocol.requestresponse.server.*
import reactor.Environment
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class RequestResponseServerProtocolStackSpec extends Specification {

    def config = new AutoConfiguration(serviceName: "tombola")
    def discovery = Mock(Discovery) {
        findService(_) >> Optional.of(new ServiceDescriptor("tombola", [], ["application/json+AES"], [], []))
    }

    def "createChannel gives a channel that calls findHandler on a message received"() {
        Environment.initializeIfEmpty()
        def handlers = Mock(RequestResponseHandlers)
        def stack = new RequestResponseServerProtocolStack(handlers, new JsonOnlyCodecs(), discovery, config)

        when:
        def channel = stack.createChannel()
        channel.send(inbound("123", "FAKESERVICE", "requestresponse"))
        Thread.sleep(50)

        then:
        1 * handlers.findHandler(_) >> Mock(RequestResponseServerHandler)
    }

    def "handler can be invoked via the external channel"() {
        Environment.initializeIfEmpty()
        def handler = Mock(RequestResponseServerHandler) {
            getRequestType() >> Map
        }
        def handlers = Mock(RequestResponseHandlers) {
            findHandler(_) >> handler

        }
        def stack = new RequestResponseServerProtocolStack(handlers, new JsonOnlyCodecs(), discovery, config)

        when:
        def channel = stack.createChannel()
        channel.send(inbound("123", "FAKESERVICE", "requestresponse"))
        Thread.sleep(50)

        then:
        1 * handler.handle(_)
    }
    def "handler can reply down the channel"() {
        Environment.initializeIfEmpty()
        def handler = Mock(RequestResponseServerHandler) {
            handle(_) >> { RequestWrapper wrapper ->
                wrapper.answer(new ServerResponse(200, "hello"))
            }
            getRequestType() >> Map
        }

        def handlers = Mock(RequestResponseHandlers) {
            findHandler(_) >> handler
        }
        def stack = new RequestResponseServerProtocolStack(handlers, new JsonOnlyCodecs(), discovery, config)

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

    def "returns protocol descriptor"() {
        def handler = Mock(RequestResponseServerHandler) {
            handle(_) >> { RequestWrapper wrapper ->
                wrapper.answer(new ServerResponse(200, "hello"))
            }
            getRequestType() >> Map
        }

        def handlers = Mock(RequestResponseHandlers) {
            findHandler(_) >> handler
            getHandlers() >> [
                    mockHandler(),
                    mockHandler(),
                    mockHandler(),
            ]
        }
        def stack = new RequestResponseServerProtocolStack(handlers, new JsonOnlyCodecs(), discovery, config)

        when:
        def protocolDescriptor = stack.protocolDescriptor

        then:
        protocolDescriptor.protocolScheme == "rpc"
        protocolDescriptor.operations.size() == 3

    }

    def mockHandler() {
        Mock(RequestResponseServerHandler) {
            getPredicate() >> HandlerPredicates.path("simples")
        }
    }

    def inbound(id, String service, String protocol) {
        MuonMessageBuilder.fromService("localService")
                .toService(service)
                .step(RRPEvents.REQUEST)
                .protocol(protocol)
                .contentType("application/json")
                .payload(new GsonCodec().encode(new Request(new URI("request://myurl"), [:])))
                .buildInbound()
    }
}


