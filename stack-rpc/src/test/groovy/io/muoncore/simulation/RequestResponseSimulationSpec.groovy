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
import io.muoncore.protocol.rpc.client.RpcClient
import io.muoncore.protocol.rpc.server.HandlerPredicates
import io.muoncore.protocol.rpc.server.RpcServer
import io.muoncore.protocol.rpc.server.ServerResponse
import reactor.Environment
import reactor.rx.broadcast.Broadcaster
import spock.lang.Specification
import spock.lang.Timeout
import spock.util.concurrent.PollingConditions

import static io.muoncore.protocol.rpc.server.HandlerPredicates.all

@Timeout(10)
class RequestResponseSimulationSpec extends Specification {

    def "many services can run and be discovered"() {
        def eventbus = new EventBus()
        given: "some services"

        def discovery = new InMemDiscovery()

        def services = (1..100).collect {
          def (Muon muon, server, client) = createService(it, discovery, eventbus)
          muon
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

        def muons = []
        def clients = []

        def services = (0..5).collect {
            def (Muon muon, RpcServer service, RpcClient client) = createService(it, discovery, eventbus)
          muons << muon
          clients << client
          service
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

        clients[0].request("request://service-1/", list).get().getPayload(Map).svc == "svc1"
        clients[0].request("request://service-2/", ["sibble"]).get().getPayload(Map).svc == "svc2"
        clients[0].request("request://service-3/", ["sibble"]).get().getPayload(Map).svc == "svc3"
        clients[0].request("request://service-4/", ["sibble"]).get().getPayload(Map).svc == "svc4"
        clients[0].request("request://service-5/", ["sibble"]).get().getPayload(Map).svc == "svc5"

        cleanup:
        muons*.shutdown()
    }

    def "promise interface works"() {
        def eventbus = new EventBus()
        Environment.initializeIfEmpty()
        StandardAsyncChannel.echoOut=true

        given: "some services"

        def data

        def discovery = new InMemDiscovery()

        def muons = []
        def clients = []

        def services = (0..5).collect {
          def (Muon muon, RpcServer service, RpcClient client) = createService(it, discovery, eventbus)
          muons << muon
          clients << client
          service
        }

        services[1].handleRequest(all()) {
            println "Request received, sending response"
            it.ok([svc:"svc1"])
        }

        when:
        clients[0].request("request://service-1/", []).then {
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
        muons*.shutdown()
        StandardAsyncChannel.echoOut=false
    }

    def "publisher interface works"() {
        def eventbus = new EventBus()
        StandardAsyncChannel.echoOut=true
        Environment.initializeIfEmpty()

        given: "some services"

        def data

        def discovery = new InMemDiscovery()

        def clients = []
        def muons = []

        def services = (0..5).collect {
          def (Muon muon, RpcServer service, RpcClient client) = createService(it, discovery, eventbus)
          muons << muon
          clients << client
          service
        }

        services[1].handleRequest(all()) {
            it.answer(new ServerResponse(200, [svc:"svc1"]))
        }

        when:

        def b = Broadcaster.<Response>create()
        b.consume {
            data = it.getPayload(Map)
        }

        clients[0].request("request://service-1/", []).toPublisher().subscribe(b)

        then:
        new PollingConditions(timeout: 5).eventually {
            data != null
            data.svc == "svc1"
        }

        cleanup:
        muons*.shutdown()
        StandardAsyncChannel.echoOut=false
    }

  def createService(ident, discovery, eventbus) {
        def config = new AutoConfiguration(serviceName: "service-${ident}")
        def transport = new InMemTransport(config, eventbus)

        def muon = new MultiTransportMuon(config, discovery, [transport], new JsonOnlyCodecs())

        [muon, new RpcServer(muon), new RpcClient(muon)]
    }
}
