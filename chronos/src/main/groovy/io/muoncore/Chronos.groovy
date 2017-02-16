package io.muoncore

import io.muoncore.config.MuonConfigBuilder
import io.muoncore.protocol.event.Event
import io.muoncore.protocol.event.server.EventServerProtocolStack
import io.muoncore.protocol.event.server.EventWrapper
import io.muoncore.protocol.reactivestream.messages.ReactiveStreamSubscriptionRequest
import io.muoncore.protocol.reactivestream.server.PublisherLookup
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.rx.Streams
import reactor.rx.broadcast.Broadcaster

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicLong

/*
 A test event store, implementing the core interfaces of Photon.
 */

class Chronos {

  static Logger log = LoggerFactory.getLogger(Chronos)

  Chronos(Muon muon) {

    List<Event> history = new ArrayList<>();
    List<Broadcaster> subs = new ArrayList<>();
    AtomicLong orderid = new AtomicLong(1)

    muon.publishGeneratedSource("/stream", PublisherLookup.PublisherType.HOT_COLD) { ReactiveStreamSubscriptionRequest request ->

      def stream = request.args["stream-name"]
      def streamType = request.args["stream-type"]
      log.info request.args
      if (!streamType) streamType = "hot-cold"

      log.info "Subscribing to $stream"

      def b = Broadcaster.<Event> create()

      BlockingQueue q = new LinkedBlockingQueue()

      subs << b

      b.map {
        if (it instanceof EventWrapper) {
          return it.event
        }
        it
      }.filter { it.streamName == stream }.consume {
        q.add(it)
      }

      if (streamType == "cold") {
        log.info "Requested a cold replay"
        return Streams.from(history.findAll {
          it.streamName == stream
        })
      }

      if (request.args["stream-type"] && streamType == "hot-cold") {
        log.debug "Has requested hot-cold replay .. "
        Thread.start {
          sleep(100)
          history.each {
            b.accept(it)
          }
        }
      }

      new Publisher() {
        @Override
        void subscribe(Subscriber s) {
          AtomicLong itemstoprocess = new AtomicLong(0)

          Thread.start {
            while (true) {
              if (itemstoprocess.get() > 0) {
                def next = q.take()
                s.onNext(next)
                itemstoprocess.decrementAndGet()
              }
              if (itemstoprocess.get() <= 0) {
                sleep(100)
              }
            }
          }

          s.onSubscribe(new Subscription() {
            @Override
            void request(long n) {
              itemstoprocess.addAndGet(n)
            }

            @Override
            void cancel() {

            }
          })
        }
      }

    }

    muon.protocolStacks.registerServerProtocol(new EventServerProtocolStack({ event ->
      log.debug "Event received " + event.event
      try {
        synchronized (history) {
          subs.stream().forEach({ q ->
            q.accept(event)
          });
          history.add(event.getEvent());
        }
        event.persisted(orderid.addAndGet(1), System.currentTimeMillis());
      } catch (Exception ex) {
        event.failed(ex.getMessage());
      }
    }, muon.getCodecs(), muon.getDiscovery()));
  }
}

