package io.muoncore.inmem

import com.google.common.eventbus.EventBus
import io.muoncore.MultiTransportMuon
import io.muoncore.Muon
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.seda.InMemSeda
import io.muoncore.memory.transport.InMemTransport
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Timeout

class IntrospectionSimulationSpec extends Specification {

    def eventbus = new EventBus()

    @Timeout(100)
    "many services can run and be introspected"() {

        given: "some services"

        def discovery = new InMemDiscovery()

        //TODO, extract this out into the SEDA stack and have a general way of using it that isn't here.
        def seda = new InMemSeda(discovery, eventbus);

        def svc1 = createService("mine", discovery)

        when:
        def names = svc1.discovery.serviceNames

        then:
        names == ["hello"]

    }

    Muon createService(ident, discovery) {
        def config = new AutoConfiguration(serviceName: "${ident}")
        def transport = new InMemTransport(config, eventbus)

        new MultiTransportMuon(config, discovery, [transport], new JsonOnlyCodecs())
    }
}
