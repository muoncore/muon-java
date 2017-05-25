package io.muoncore.perf

import com.google.common.eventbus.EventBus
import io.muoncore.InstanceDescriptor
import io.muoncore.MultiTransportMuon
import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.descriptors.ProtocolDescriptor
import io.muoncore.descriptors.SchemaDescriptor
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonOutboundMessage
import io.muoncore.protocol.ServerProtocolStack
import reactor.Environment
import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import java.util.concurrent.TimeUnit

@IgnoreIf({ System.getenv("SHORT_TEST") })
@Timeout(10)
class ChannelPerfSpec extends Specification {

   /* def eventbus = new EventBus()

    def setup() {
        Environment.initializeIfEmpty()
    }

    @Unroll
    def "#numTimes channels can run concurrently"() {
        given: "some services"

        def discovery = new InMemDiscovery()

        def service1 = createService("1", discovery)
        def service2 = createService("2", discovery)

        discovery.advertiseLocalService(new InstanceDescriptor("instance-123", "service-1", [], [], [], []))
        discovery.advertiseLocalService(new InstanceDescriptor("instance-12345", "service-2", [], [], [], []))

        service2.handleRequest(all()) {
            it.ok([svc:"svc1"])
        }

        when:
        def requests = []

        numTimes.times {
            requests << service1.request("request://service-2/", [])
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
            ChannelConnection<MuonInboundMessage, MuonOutboundMessage> createChannel() {
                return new ChannelConnection<MuonInboundMessage, MuonOutboundMessage>() {
                    @Override
                    void receive(ChannelConnection.ChannelFunction<MuonOutboundMessage> function) {

                    }

                    @Override
                    void send(MuonInboundMessage message) {
                        data << message
                    }

                    @Override
                    void shutdown() {
                    }
                }
            }

          @Override
          Map<String, SchemaDescriptor> getSchemasFor(String endpoint) {
            return null
          }
        })

        def service2 = createService("2", discovery)

        discovery.advertiseLocalService(new InstanceDescriptor("instance-123", "service-1", [], [], [], []))
        discovery.advertiseLocalService(new InstanceDescriptor("instance-321", "service-2", [], [], [], []))

        when:
        def channel = service2.transportClient.openClientChannel()
        channel.receive {
            //println "Data coming back?"
        }

        println "Starting message emit"
        numTimes.times {
            channel.send(
                    MuonMessageBuilder.fromService("service-1")
                        .toService("service-1")
                    .protocol("fake-proto")
                    .contentType("text/plain")
                    .payload(new byte[0])
                    .step("somethingHappened")
                    .build()
            )
        }
        println "Message emit completed"

        then:

        new PollingConditions(timeout: 30).eventually {
            data.size() == numTimes
        }

        cleanup:
        service1.shutdown()
        service2.shutdown()

        where:
        numTimes << [500, 2000, 5000, 10000]
    }

    MultiTransportMuon createService(ident, discovery) {
        def config = new AutoConfiguration(serviceName: "service-${ident}")
        def transport = new InMemTransport(config, eventbus)

        new MultiTransportMuon(config, discovery, [transport], new JsonOnlyCodecs())
    }*/
}
