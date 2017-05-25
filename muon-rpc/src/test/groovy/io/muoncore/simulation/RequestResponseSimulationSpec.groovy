package io.muoncore.simulation

import com.google.common.eventbus.EventBus
import io.muoncore.MultiTransportMuon
import io.muoncore.Muon
import io.muoncore.channel.impl.StandardAsyncChannel
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.protocol.rpc.Response
import io.muoncore.protocol.rpc.server.ServerResponse
import reactor.Environment
import reactor.rx.broadcast.Broadcaster
import spock.lang.Specification
import spock.lang.Timeout
import spock.util.concurrent.PollingConditions

@Timeout(10)
class RequestResponseSimulationSpec extends Specification {

    def "many services can run and be discovered"() {
        def eventbus = new EventBus()
        given: "some services"

        def discovery = new InMemDiscovery()

        def services = (1..100).collect {
            createService(it, discovery, eventbus)
        }

        expect:
        discovery.serviceNames.size() == 100

        cleanup:
        services*.shutdown()
    }

    def "1 service can make requests to 5 others"() {
        def eventbus = new EventBus()
        Environment.initializeIfEmpty()

        given: "some services"

        def discovery = new InMemDiscovery()

        def services = (0..5).collect {
            createService(it, discovery, eventbus)
        }

        services[1].handleRequest(all()) {
            it.ok([svc:"svc1"])
        }
        services[2].handleRequest(all()) {
            it.ok([svc:"svc2"])
        }
        services[3].handleRequest(all()) {
            it.ok([svc:"svc3"])
        }
        services[4].handleRequest(all()) {
            it.ok([svc:"svc4"])
        }
        services[5].handleRequest(all()) {
            it.ok([svc:"svc5"])
        }

        expect:

        def list = new ArrayList<String>()
        list << "helloww"

        services[0].request("request://service-1/", list).get().getPayload(Map).svc == "svc1"
        services[0].request("request://service-2/", ["sibble"]).get().getPayload(Map).svc == "svc2"
        services[0].request("request://service-3/", ["sibble"]).get().getPayload(Map).svc == "svc3"
        services[0].request("request://service-4/", ["sibble"]).get().getPayload(Map).svc == "svc4"
        services[0].request("request://service-5/", ["sibble"]).get().getPayload(Map).svc == "svc5"

        cleanup:
        services*.shutdown()
    }

    def "promise interface works"() {
        def eventbus = new EventBus()
        Environment.initializeIfEmpty()
        StandardAsyncChannel.echoOut=true

        given: "some services"

        def data

        def discovery = new InMemDiscovery()

        def services = (0..5).collect {
            createService(it, discovery, eventbus)
        }

        services[1].handleRequest(all()) {
            println "Request received, sending response"
            it.ok([svc:"svc1"])
        }

        when:
        services[0].request("request://service-1/", []).then {
            println "Response says something."
            data = it.getPayload(Map)
        }

        then:
        new PollingConditions().eventually {

            data != null
            data.svc == "svc1"
        }

        cleanup:
        println "Data is ${data}"
        System.out.flush()
        services*.shutdown()
        StandardAsyncChannel.echoOut=false
    }

    def "publisher interface works"() {
        def eventbus = new EventBus()
        StandardAsyncChannel.echoOut=true
        Environment.initializeIfEmpty()

        given: "some services"

        def data

        def discovery = new InMemDiscovery()

        def services = (0..5).collect {
            createService(it, discovery, eventbus)
        }

        services[1].handleRequest(all()) {
            it.answer(new ServerResponse(200, [svc:"svc1"]))
        }

        when:

        def b = Broadcaster.<Response>create()
        b.consume {
            data = it.getPayload(Map)
        }

        services[0].request("request://service-1/", []).toPublisher().subscribe(b)

        then:
        new PollingConditions(timeout: 5).eventually {
            data != null
            data.svc == "svc1"
        }

        cleanup:
        services*.shutdown()
        StandardAsyncChannel.echoOut=false
    }

    Muon createService(ident, discovery, eventbus) {
        def config = new AutoConfiguration(serviceName: "service-${ident}")
        def transport = new InMemTransport(config, eventbus)

        new MultiTransportMuon(config, discovery, [transport], new JsonOnlyCodecs())
    }
}
