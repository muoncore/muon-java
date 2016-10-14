package com.simplicityitself

import io.muoncore.MuonBuilder
import io.muoncore.config.MuonConfigBuilder
import io.muoncore.protocol.event.Event
import io.muoncore.protocol.event.server.EventServerProtocolStack
import io.muoncore.protocol.event.server.EventWrapper
import io.muoncore.protocol.reactivestream.messages.ReactiveStreamSubscriptionRequest
import io.muoncore.protocol.reactivestream.server.PublisherLookup
import reactor.rx.broadcast.Broadcaster
/*
 A toy event store, implementing the core interfaces of Photon.
 */
class Chronos {

    static main(args) {

        def config = MuonConfigBuilder.withServiceIdentifier("chronos")
                            .withTags("eventstore", "chronos", "photon").build()

        def muon = MuonBuilder.withConfig(config).build()

        List<Event> history = new ArrayList<>();
        List<Broadcaster> subs = new ArrayList<>();

        muon.publishGeneratedSource("/stream", PublisherLookup.PublisherType.HOT_COLD) { ReactiveStreamSubscriptionRequest request ->

            def stream = request.args["stream-name"]

            println "Subscribing to $stream"

            def b = Broadcaster.<Event>create()

            //this potentially loses messages during the window from returning here and the item subscribing.
            //this gives reasonable behaviour, but explicitly does not handle enforcing the order of hot vs cold messages.
            //it's possible for them to interleave here, as we don't buffer up the hot ones during cold replay.
            b.observeSubscribe {
                history.each {
                    b.accept(it)
                }
            }

            subs << b

            b.map {
                if (it instanceof EventWrapper) {
                    return it.event
                }
                it
            }. filter { it.streamName == stream }
        }

        muon.getProtocolStacks().registerServerProtocol(new EventServerProtocolStack( { event ->
            System.out.println("Event received")
            try {
                synchronized (history) {
                    subs.stream().forEach({ q ->
                        q.accept(event)
                    });
                    history.add(event.getEvent());
                }
                event.persisted(12313, System.currentTimeMillis());
            } catch (Exception ex) {
                event.failed(ex.getMessage());
            }
        }, muon.getCodecs(), muon.getDiscovery()));
    }
}

