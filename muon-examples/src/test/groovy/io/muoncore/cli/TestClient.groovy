package io.muoncore.cli

import io.muoncore.Muon
import io.muoncore.MuonBuilder
import io.muoncore.config.MuonConfigBuilder
import io.muoncore.protocol.event.client.EventClient
/**
 * Created by david on 01/04/16.
 */
class TestClient {

    static Muon muon
    static EventClient evclient

    static def main(args) {

        def config = MuonConfigBuilder.withServiceIdentifier("client").build()

        muon = MuonBuilder.withConfig(config).build()

        muon.discovery.blockUntilReady()

        def data = muon.request("rpc://config-service/uuid").get()

        println "Server says ${data}"
        println "Server says ${data.getPayload(String)}"

//        muon.introspect("photon").then {
//            it.protocols.each {
//                println "PROTO - ${it.protocolName}"
//                it.operations.each {
//                    println "${it.resource} -- ${it.doc}"
//                }
//            }
//        }

//        evclient = new DefaultEventClient(muon)

//        evclient.getProjectionList().get().each {
//            println "PROJECTION - ${it}"
//        }



//        println "SIMPLE IS ${evclient.getProjection("__streams__", Map).get().currentState["current-value"]}"
//        println "SIMPLE IS ${evclient.getProjection("counter", Map).get().currentState["current-value"]}"

//
////        publishEventRpc()
////
//        def sub = Broadcaster.create()
//        sub.consume {
//            println "EVENT = ${it}"
//        }
//        evclient.replay("rahrah", EventReplayMode.REPLAY_THEN_LIVE, sub)

//        while(true) {
//
////            sleep(500)
//            evclient.event(new ClientEvent( "awesome",
//                    "rahrah", null, null, null, [
//                    "hello": "world",
//                    "awesome": "times"
//            ]
//            ))
//        }
    }
}
