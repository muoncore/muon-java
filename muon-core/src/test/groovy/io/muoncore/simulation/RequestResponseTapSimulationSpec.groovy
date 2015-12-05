package io.muoncore.simulation
import com.google.common.eventbus.EventBus
import io.muoncore.Muon
import io.muoncore.SingleTransportMuon
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.protocol.requestresponse.Response
import io.muoncore.transport.TransportMessage
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all
//@Timeout(4)
class RequestResponseTapSimulationSpec extends Specification {

    def eventbus = new EventBus()

    def "many services can run and the data can be tapped"() {
        given: "some services"

        def discovery = new InMemDiscovery()

        def services = (0..5).collect {
            createService(it, discovery)
        }

        List<TransportMessage> data = []
        services[0].transportControl.tap({
            true
        }).subscribe(new Subscriber<TransportMessage>() {
            @Override
            void onSubscribe(Subscription s) {
                s.request(200)
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

        services[1].handleRequest(all(), Map) {
            it.answer(new Response(200, [svc:"svc1"]))
        }
        services[1].handleRequest(all(), Map) {
            it.answer(new Response(200, [svc:"svc2"]))
        }
        services[3].handleRequest(all(), Map) {
            it.answer(new Response(200, [svc:"svc3"]))
        }
        services[4].handleRequest(all(), Map) {
            it.answer(new Response(200, [svc:"svc4"]))
        }
        services[5].handleRequest(all(), Map) {
            it.answer(new Response(200, [svc:"svc5"]))
        }

        def dat = []

        when:

        dat << services[0].introspect("service-1").get()
        dat << services[0].request("request://service-1/", [], Map).get()
        dat << services[0].request("request://service-2/", [], Map).get()
        dat << services[0].request("request://service-3/", [], Map).get()

        then:
        new PollingConditions(timeout: 3).eventually {
            dat.size() > 0 &&
            data.size() == 8 &&
            data[0].protocol == "introspect" &&
            data[2].protocol == "request"
        }

        cleanup:
        services*.shutdown()
    }

    Muon createService(ident, discovery) {
        def config = new AutoConfiguration(serviceName: "service-${ident}", aesEncryptionKey: "abcde12345678906")
        def transport = new InMemTransport(config, eventbus)

        new SingleTransportMuon(config, discovery, transport)
    }
}
