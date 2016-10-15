package com.simplicityitself

import io.muoncore.MuonBuilder
import io.muoncore.config.MuonConfigBuilder
import io.muoncore.protocol.event.Event
import io.muoncore.protocol.event.server.EventServerProtocolStack
import io.muoncore.protocol.event.server.EventWrapper
import io.muoncore.protocol.reactivestream.messages.ReactiveStreamSubscriptionRequest
import io.muoncore.protocol.reactivestream.server.PublisherLookup
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import reactor.rx.broadcast.Broadcaster

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

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

            BlockingQueue q = new LinkedBlockingQueue()

            subs << b

            b.map {
                if (it instanceof EventWrapper) {
                    return it.event
                }
                it
            }. filter { it.streamName == stream }.consume {
                q.add(it)
            }

            if (request.args["stream-type"] &&  request.args["stream-type"] in ["cold", "hot-cold"]) {
                println "Has requested replay .. "
                history.each {
                    b.accept(it)
                }
            }

            new Publisher() {
                @Override
                void subscribe(Subscriber s) {
                    Thread.start {
                        while(true) {
                            def next = q.take()
                            println "Sending $next"
                            s.onNext(next)
                        }
                    }
                }
            }
        }

        muon.getProtocolStacks().registerServerProtocol(new EventServerProtocolStack( { event ->
            System.out.println("Event received " + event.event)
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

