package io.muoncore.protocol.requestresponse

import io.muoncore.Discovery
import io.muoncore.ServiceDescriptor
import io.muoncore.channel.Channels
import io.muoncore.codec.Codecs
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonOutboundMessage
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocolStack
import io.muoncore.protocol.requestresponse.server.*
import io.muoncore.channel.support.Scheduler
import io.muoncore.transport.client.TransportClient
import reactor.Environment
import spock.lang.Specification
import spock.lang.Timeout

import java.util.function.Predicate

class RequestResponseClientServerIntegrationSpec extends Specification {

    def discovery = Mock(Discovery) {
        findService(_) >> Optional.of(new ServiceDescriptor("tombola", [], ["application/json+AES"], []))
    }

    @Timeout(2)
    def "client and server can communicate"() {
        given:
        Environment.initializeIfEmpty()
        def handlers = new DynamicRequestResponseHandlers(new RequestResponseServerHandler() {
            @Override
            HandlerPredicate getPredicate() {
                return HandlerPredicates.none()
            }

            @Override
            void handle(RequestWrapper request) {
                request.answer(new ServerResponse(200, [message:"defaultservice"]))
            }

        })
        handlers.addHandler(new RequestResponseServerHandler() {
            @Override
            HandlerPredicate getPredicate() {
                return new HandlerPredicate() {
                    @Override
                    String resourceString() {
                        return ""
                    }

                    @Override
                    Predicate<ServerRequest> matcher() {
                        return { it.url.host == "someapp" }
                    }
                }
            }

            @Override
            void handle(RequestWrapper request) {
                request.answer(new ServerResponse(200, [message:"hello"]))
            }

        })

        def server = new RequestResponseServerProtocolStack(handlers, new JsonOnlyCodecs(), discovery, new AutoConfiguration(serviceName: "simples"))

        def channel = Channels.channel("left", "right")

        //does the conversion from outbound<=>inbound that the transport pair would ordinarily do.
        Channels.connectAndTransform(
                server.createChannel(),
                channel.left(),
                { MuonOutboundMessage msg ->
                    if (msg == null) return null
                    MuonMessageBuilder
                            .fromService(msg.targetServiceName)
                            .toService(msg.targetServiceName)
                            .step(msg.step)
                            .status(msg.status)
                            .protocol(msg.protocol)
                            .contentType(msg.contentType)
                            .payload(msg.payload)
                            .operation(msg.channelOperation)
                            .buildInbound()

                },
                { MuonOutboundMessage msg ->
                    if (msg == null) return null
                    MuonMessageBuilder
                            .fromService(msg.targetServiceName)
                            .toService(msg.targetServiceName)
                            .step(msg.step)
                            .status(msg.status)
                            .protocol(msg.protocol)
                            .contentType(msg.contentType)
                            .payload(msg.payload)
                            .operation(msg.channelOperation)
                            .buildInbound()
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
                return new AutoConfiguration(serviceName: "remote")
            }

            @Override
            Scheduler getScheduler() {
                return new Scheduler()
            }
        }

        when:
        Response response = client.request(
                new Request(new URI("request://someapp"), [message:"yoyo"])).get()

        then:
        response.status == 200
        response.getPayload(Map)?.message == "hello"
    }
}
