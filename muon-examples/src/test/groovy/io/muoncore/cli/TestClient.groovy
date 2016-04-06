package io.muoncore.cli

import io.muoncore.Muon
import io.muoncore.MuonBuilder
import io.muoncore.config.MuonConfigBuilder
import io.muoncore.protocol.event.client.DefaultEventClient
import io.muoncore.protocol.event.client.EventClient
import io.muoncore.protocol.event.client.EventReplayMode
import reactor.rx.broadcast.Broadcaster
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

        muon.introspect("photon").then {
            it.protocols.each {
                println "PROTO - ${it.protocolName}"
                it.operations.each {
                    println "${it.resource} -- ${it.doc}"
                }
            }
        }

        evclient = new DefaultEventClient(muon)

//        publishEventRpc()

        def sub = Broadcaster.create()
        sub.consume {
            println "EVENT = ${it}"
        }
        evclient.replay("awesome", EventReplayMode.LIVE_ONLY, sub)

//        while(true) {
//
//            sleep(500)
//            evclient.event("awesome", new Event(
//                    "rahrah", "MYEVENTID", "", "client", [
//                    "hello": "world",
//                    "awesome": "times"
//            ]
//            ))
//        }
    }
}
