package io.muoncore.simulation

import com.google.common.eventbus.EventBus
import io.muoncore.Muon
import io.muoncore.SingleTransportMuon
import io.muoncore.channel.async.StandardAsyncChannel
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.protocol.requestresponse.Response
import reactor.Environment
import reactor.rx.broadcast.Broadcaster
import spock.lang.Specification
import spock.lang.Timeout
import spock.util.concurrent.PollingConditions

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all

@Timeout(1)
class RequestResponseSimulationSpec extends Specification {

    def "many services can run and be discovered"() {
        def eventbus = new EventBus()
        given: "some services"

        def discovery = new InMemDiscovery()

        def services = (1..100).collect {
            createService(it, discovery, eventbus)
        }

        expect:
        discovery.knownServices.size() == 100

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

        services[0].request("request://service-1/", [], Map).get().payload.svc == "svc1"
        services[0].request("request://service-2/", [], Map).get().payload.svc == "svc2"
        services[0].request("request://service-3/", [], Map).get().payload.svc == "svc3"
        services[0].request("request://service-4/", [], Map).get().payload.svc == "svc4"
        services[0].request("request://service-5/", [], Map).get().payload.svc == "svc5"

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

        services[1].handleRequest(all(), Map) {
            println "Request received, sending response"
            it.answer(new Response(200, [svc:"svc1"]))
        }

        when:
        services[0].request("request://service-1/", [], Map).then {
            println "Response says something."
            data = it.payload
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

        services[1].handleRequest(all(), Map) {
            it.answer(new Response(200, [svc:"svc1"]))
        }

        when:

        def b = Broadcaster.create()
        b.consume {
            data = it.payload
        }

        services[0].request("request://service-1/", [], Map).toPublisher().subscribe(b)

        then:
        new PollingConditions().eventually {
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

        new SingleTransportMuon(config, discovery, transport)
    }
}
