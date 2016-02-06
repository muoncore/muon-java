package io.muoncore.simulation

import com.google.common.eventbus.EventBus
import io.muoncore.Muon
import io.muoncore.SingleTransportMuon
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.protocol.introspection.server.IntrospectionServerProtocolStack
import io.muoncore.protocol.requestresponse.RRPTransformers
import io.muoncore.protocol.requestresponse.Response
import spock.lang.Specification
import spock.lang.Timeout

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all

@Timeout(1)
class IntrospectionSimulationSpec extends Specification {

    def eventbus = new EventBus()

    def "many services can run and be introspected"() {
        given: "some services"

        def discovery = new InMemDiscovery()

        def services = (0..5).collect {
            createService(it, discovery)
        }

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

        when:
        def descriptor = services[0].introspect("service-1").get()

        then:
        descriptor.serviceName == "service-1"
        descriptor.protocols.protocolScheme.contains(RRPTransformers.REQUEST_RESPONSE_PROTOCOL)
        descriptor.protocols.protocolScheme.contains(IntrospectionServerProtocolStack.PROTOCOL)
        descriptor.protocols.find { it.protocolScheme == RRPTransformers.REQUEST_RESPONSE_PROTOCOL }.operations.size() == 2
        cleanup:
        services*.shutdown()
    }

    Muon createService(ident, discovery) {
        def config = new AutoConfiguration(serviceName: "service-${ident}")
        def transport = new InMemTransport(config, eventbus)

        new SingleTransportMuon(config, discovery, transport)
    }
}
