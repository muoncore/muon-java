package io.muoncore.inmem.discovery

import com.google.common.eventbus.EventBus
import io.muoncore.Muon
import io.muoncore.SingleTransportMuon
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.protocol.requestresponse.Response
import spock.lang.Specification
import spock.lang.Timeout

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all

@Timeout(1)
class ExampleSimulationSpec extends Specification {

    def eventbus = new EventBus()

    def "many services can run and be discovered"() {
        given: "some services"

        def discovery = new InMemDiscovery()

        def services = (1..100).collect {
            createService(it, discovery)
        }

        expect:
        discovery.knownServices.size() == 100

        cleanup:
        services*.shutdown()
    }

    def "1 service can make requests to 5 others"() {
        given: "some services"

        def discovery = new InMemDiscovery()

        def services = (0..5).collect {
            createService(it, discovery)
        }

        services[1].handleRequest(all(), Map) {
            it.answer(new Response(200, [svc:"svc1"]))
        }
        services[2].handleRequest(all(), Map) {
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

        expect:
        services[0].request("muon://service-1/", [], Map).get().payload.svc == "svc1"
        services[0].request("muon://service-2/", [], Map).get().payload.svc == "svc2"
        services[0].request("muon://service-3/", [], Map).get().payload.svc == "svc3"
        services[0].request("muon://service-4/", [], Map).get().payload.svc == "svc4"
        services[0].request("muon://service-5/", [], Map).get().payload.svc == "svc5"

        cleanup:
        services*.shutdown()
    }

    Muon createService(ident, discovery) {
        def config = new AutoConfiguration(serviceName: "service-${ident}")
        def transport = new InMemTransport(config, eventbus)

        new SingleTransportMuon(config, discovery, transport)
    }
}
