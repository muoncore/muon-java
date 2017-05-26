package io.muoncore.protocol.rpc

import io.muoncore.Discovery
import io.muoncore.Muon
import io.muoncore.ServiceDescriptor
import io.muoncore.channel.Channels
import io.muoncore.channel.support.Scheduler
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.descriptors.SchemaDescriptor
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonOutboundMessage
import io.muoncore.protocol.rpc.client.RpcClient
import io.muoncore.protocol.rpc.server.DynamicRequestResponseHandlers
import io.muoncore.protocol.rpc.server.HandlerPredicate
import io.muoncore.protocol.rpc.server.HandlerPredicates
import io.muoncore.protocol.rpc.server.RequestResponseServerHandler
import io.muoncore.protocol.rpc.server.RequestResponseServerProtocolStack
import io.muoncore.protocol.rpc.server.RequestWrapper
import io.muoncore.protocol.rpc.server.ServerRequest
import io.muoncore.protocol.rpc.server.ServerResponse
import io.muoncore.transport.client.TransportClient
import reactor.Environment
import spock.lang.Specification
import spock.lang.Timeout

import java.util.function.Predicate

class RequestResponseClientServerIntegrationSpec extends Specification {

    TransportClient client
    Muon muon

    def setup() {
      client = Mock(TransportClient)
      muon = Mock(Muon) {
        getDiscovery() >> Mock(Discovery) {
          getCodecsForService(_) >> ["application/json"]
        }
        getTransportClient() >> client
        getConfiguration() >> Mock(AutoConfiguration)
        getCodecs() >> new JsonOnlyCodecs()
        getScheduler() >> new Scheduler()
      }
    }

    def discovery = Mock(Discovery) {
        getServiceNamed(_) >> Optional.of(new ServiceDescriptor("tombola", [], ["application/json+AES"], [], []))
    }

    @Timeout(15)
    def "client gets a timeout if the server is slow"() {
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

          @Override
          Map<String, SchemaDescriptor> getDescriptors() {
            return Collections.emptyMap()
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
                sleep(11000)
                request.answer(new ServerResponse(200, [message:"hello"]))
            }

          @Override
          Map<String, SchemaDescriptor> getDescriptors() {
            return Collections.emptyMap()
          }
        })

        def server = new RequestResponseServerProtocolStack(handlers, muon)

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

        client.openClientChannel() >> channel.right()


        def client = new RpcClient(muon)
        when:
        Response response = client.request(
                new Request(new URI("request://someapp"), [message:"yoyo"])).get()

        then:
        response.status == 408
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

          @Override
          Map<String, SchemaDescriptor> getDescriptors() {
            return Collections.emptyMap()
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
                        return {
                          println "TESTING ${it.properties}"
                          it.url.host == "someapp" }
                    }
                }
            }

            @Override
            void handle(RequestWrapper request) {
                request.answer(new ServerResponse(200, [message:"hello"]))
            }

          @Override
          Map<String, SchemaDescriptor> getDescriptors() {
            return Collections.emptyMap()
          }
        })

        def server = new RequestResponseServerProtocolStack(handlers, muon)

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

        client.openClientChannel() >> channel.right()

        def client = new RpcClient(muon)

        when:
        Response response = client.request(
                new Request(new URI("request://someapp"), [message:"yoyo"])).get()

        then:
        response.status == 200
        response.getPayload(Map)?.message == "hello"
    }
}
