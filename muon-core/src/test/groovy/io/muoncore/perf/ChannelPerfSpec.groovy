package io.muoncore.perf

import com.google.common.eventbus.EventBus
import io.muoncore.Muon
import io.muoncore.SingleTransportMuon
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.protocol.requestresponse.Response
import io.muoncore.transport.TransportMessage
import io.muoncore.transport.TransportOutboundMessage
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Unroll

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all

@Timeout(2)
@Ignore
class ChannelPerfSpec extends Specification {

    def eventbus = new EventBus()

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
        requests*.get().size() == numTimes

        cleanup:
        service1.shutdown()
        service2.shutdown()

        where:
        numTimes << [500, 2000, 5000, 10000, 50000]
    }

    @Unroll
    def "channels throughput can handle #numTimes messages"() {
        given: "some services"

        def data = []

        def discovery = new InMemDiscovery()

        def service1 = createService("1", discovery)
        def service2 = createService("2", discovery)
        service1.transportControl.tap( { }).subscribe(new Subscriber<TransportMessage>() {
            @Override
            void onSubscribe(Subscription s) {
                s.request(Integer.MAX_VALUE)
            }

            @Override
            void onNext(TransportMessage transportMessage) {
                data << transportMessage
            }

            @Override
            void onError(Throwable t) {

            }

            @Override
            void onComplete() {

            }
        })

        when:
        def requests = []
        def channel = service2.transportClient.openClientChannel()

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

        requests*.get().size() == numTimes

        cleanup:
        service1.shutdown()
        service2.shutdown()

        where:
        numTimes << [500, 2000, 5000, 10000, 50000]
    }

    Muon createService(ident, discovery) {
        def config = new AutoConfiguration(serviceName: "service-${ident}", aesEncryptionKey: "abcde12345678906")
        def transport = new InMemTransport(config, eventbus)

        new SingleTransportMuon(config, discovery, transport)
    }
}
