package io.muoncore.simulation

import com.google.common.eventbus.EventBus
import io.muoncore.MultiTransportMuon
import io.muoncore.Muon
import io.muoncore.channel.impl.StandardAsyncChannel
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.message.MuonMessage
import io.muoncore.protocol.rpc.client.RpcClient
import io.muoncore.protocol.rpc.server.HandlerPredicates
import io.muoncore.protocol.rpc.server.RpcServer
import io.muoncore.protocol.rpc.server.ServerResponse
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import static io.muoncore.protocol.rpc.server.HandlerPredicates.all

//@Timeout(4)
class RequestResponseTapSimulationSpec extends Specification {

    def eventbus = new EventBus()

    def "many services can run and the data can be tapped"() {
        StandardAsyncChannel.echoOut=true
        given: "some services"

        def discovery = new InMemDiscovery()

        def services = (0..5).collect {
            createService(it, discovery)
        }

        def servers = services.collect {
          new RpcServer(it)
        }

        def clients = services.collect {
          new RpcClient(it)
        }

        List<MuonMessage> data = []
        services[0].transportControl.tap({
            true
        }).subscribe(new Subscriber<MuonMessage>() {
            @Override
            void onSubscribe(Subscription s) {
                s.request(200)
            }

            @Override
            void onNext(MuonMessage transportMessage) {
                data << transportMessage
            }

            @Override
            void onError(Throwable t) {

            }

            @Override
            void onComplete() {

            }
        })

        servers[1].handleRequest(all()) {
            it.answer(new ServerResponse(200, [svc:"svc1"]))
        }
        servers[1].handleRequest(all()) {
            it.answer(new ServerResponse(200, [svc:"svc2"]))
        }
        servers[3].handleRequest(all()) {
            it.answer(new ServerResponse(200, [svc:"svc3"]))
        }
        servers[4].handleRequest(all()) {
            it.answer(new ServerResponse(200, [svc:"svc4"]))
        }
        servers[5].handleRequest(all()) {
            it.answer(new ServerResponse(200, [svc:"svc5"]))
        }

        def dat = []

        when:

        dat << services[0].introspect("service-1").get()
        dat << clients[0].request("request://service-1/", []).get()
        dat << clients[0].request("request://service-2/", []).get()
        dat << clients[0].request("request://service-3/", []).get()

        then:
        new PollingConditions(timeout: 3).eventually {
            dat.size() > 0 &&
            data.size() == 8 &&
            data[0].protocol == "introspect" &&
            data[2].protocol == "rpc"
        }

        cleanup:
        services*.shutdown()
    }

    Muon createService(ident, discovery) {
        def config = new AutoConfiguration(serviceName: "service-${ident}")
        def transport = new InMemTransport(config, eventbus)

        new MultiTransportMuon(config, discovery, [transport], new JsonOnlyCodecs())
    }
}
