package io.muoncore.perf
import com.google.common.eventbus.EventBus
import io.muoncore.SingleTransportMuon
import io.muoncore.channel.ChannelConnection
import io.muoncore.config.AutoConfiguration
import io.muoncore.descriptors.ProtocolDescriptor
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.protocol.ServerProtocolStack
import io.muoncore.protocol.requestresponse.Response
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportOutboundMessage
import reactor.Environment
import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import java.util.concurrent.TimeUnit

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all

@Timeout(10)
//@Ignore
class ChannelPerfSpec extends Specification {

    def eventbus = new EventBus()

    def setup() {
        Environment.initializeIfEmpty()
    }

    @Unroll
    def "#numTimes channels can run concurrently"() {
        given: "some services"

        def discovery = new InMemDiscovery()

        def service1 = createService("1", discovery)
        def service2 = createService("2", discovery)

        service2.handleRequest(all(), Map) {
            it.answer(new Response(200, [svc:"svc1"]))
        }

        when:
        def requests = []

        numTimes.times {
            requests << service1.request("request://service-2/", [], Map)
        }

        then:
        requests*.get(15, TimeUnit.SECONDS).size() == numTimes

        cleanup:
        service1.shutdown()
        service2.shutdown()

        where:
        numTimes << [500, 2000]
    }

    @Unroll
    def "channels throughput can handle #numTimes messages"() {
        given: "some services"

        def data = []

        def discovery = new InMemDiscovery()

        def service1 = createService("1", discovery)
        service1.protocolStacks.registerServerProtocol(new ServerProtocolStack() {
            @Override
            ProtocolDescriptor getProtocolDescriptor() {
                return new ProtocolDescriptor("fake-proto", "fake", "no description", [])
            }

            @Override
            ChannelConnection<TransportInboundMessage, TransportOutboundMessage> createChannel() {
                return new ChannelConnection<TransportInboundMessage, TransportOutboundMessage>() {
                    @Override
                    void receive(ChannelConnection.ChannelFunction<TransportOutboundMessage> function) {

                    }

                    @Override
                    void send(TransportInboundMessage message) {
                        data << message
                    }

                    @Override
                    void shutdown() {
                    }
                }
            }
        })

        def service2 = createService("2", discovery)

        when:
        def channel = service2.transportClient.openClientChannel()
        channel.receive {
            //println "Data coming back?"
        }

        numTimes.times {
            channel.send(new TransportOutboundMessage(
                    "somethingHappened",
                    UUID.randomUUID().toString(),
                    "service-1",
                    "service-1",
                    "fake-proto",
                    [:],
                    "text/plain",
                    new byte[0],
                    ["application/json"]
            ))
        }

        then:

        new PollingConditions().eventually {
            data.size() == numTimes
        }

        cleanup:
        service1.shutdown()
        service2.shutdown()

        where:
        numTimes << [500, 2000, 5000, 10000, 50000, 1000000]
    }

    SingleTransportMuon createService(ident, discovery) {
        def config = new AutoConfiguration(serviceName: "service-${ident}", aesEncryptionKey: "abcde12345678906")
        def transport = new InMemTransport(config, eventbus)

        new SingleTransportMuon(config, discovery, transport)
    }
}
