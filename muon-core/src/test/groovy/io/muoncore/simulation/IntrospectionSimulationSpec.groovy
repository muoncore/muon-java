package io.muoncore.simulation

import com.google.common.eventbus.EventBus
import io.muoncore.MultiTransportMuon
import io.muoncore.Muon
import io.muoncore.channel.impl.StandardAsyncChannel
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.protocol.introspection.server.IntrospectionServerProtocolStack
import io.muoncore.protocol.requestresponse.RRPTransformers
import io.muoncore.protocol.requestresponse.server.ServerResponse
import spock.lang.Specification
import spock.lang.Timeout

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all


class IntrospectionSimulationSpec extends Specification {

    def eventbus = new EventBus()

    @Timeout(1)
    def "many services can run and be introspected"() {
        given: "some services"

        def discovery = new InMemDiscovery()

        def services = (0..5).collect {
            createService(it, discovery)
        }

        services[1].handleRequest(all()) {
            it.answer(new ServerResponse(200, [svc:"svc1"]))
        }
        services[1].handleRequest(all()) {
            it.answer(new ServerResponse(200, [svc:"svc2"]))
        }
        services[3].handleRequest(all()) {
            it.answer(new ServerResponse(200, [svc:"svc3"]))
        }
        services[4].handleRequest(all()) {
            it.answer(new ServerResponse(200, [svc:"svc4"]))
        }
        services[5].handleRequest(all()) {
            it.answer(new ServerResponse(200, [svc:"svc5"]))
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

    def "im mem doesn't blow up when run twice"() {
        given: "some services"
        StandardAsyncChannel.echoOut=true

        def discovery = new InMemDiscovery()

        def service = createService("service-1", discovery)
        def service1 = createService("tombola", discovery)

        service.handleRequest(all()) {
            it.answer(new ServerResponse(200, [svc:"svc1"]))
        }

        Thread.sleep(4000)

        when:
        def descriptor = service.introspect("tombola").get()
        service.shutdown()

        sleep(40000)

        println "Starting second run"
        service = createService("service-1", discovery)
        service1 = createService("tombola", discovery)

        then:
        service.introspect("service-1").get()

        cleanup:
        service.shutdown()
    }

    Muon createService(ident, discovery) {
        def config = new AutoConfiguration(serviceName: "service-${ident}")
        def transport = new InMemTransport(config, eventbus)

        new MultiTransportMuon(config, discovery, [transport])
    }
}
