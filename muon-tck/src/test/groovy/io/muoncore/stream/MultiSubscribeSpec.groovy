package io.muoncore.stream

import io.muoncore.Muon
import io.muoncore.extension.amqp.AmqpTransportExtension
import io.muoncore.extension.amqp.discovery.AmqpDiscovery
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import reactor.rx.Streams
import reactor.rx.broadcast.Broadcaster
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.Executors

class MultiSubscribeSpec extends Specification {

  def "Multiple muon based subscribers can open the same channel"() {

    given:
    def muon = new Muon(new AmqpDiscovery("amqp://localhost"))
    muon.serviceIdentifer = "eventsource"
    new AmqpTransportExtension("amqp://localhost").extend(muon)
    def pub = Broadcaster.create()
    muon.start()

    muon.streamSource("/core", Map, pub)

    Thread.sleep(3500)

    def items = []
    int concurrentSubs = 20

    concurrentSubs.times {
      def stream = Broadcaster.create()

      stream.observeError(Exception) { e, b ->
        println "Error!"
      }.consume {
          items << it
      }
      muon.subscribe("muon://eventsource/core", Map, stream)
    }

    and: "Wait for some keep-alive expiry"
    Thread.sleep(12000)

    when:
    pub.accept(["message":"is awesome"])

    then:
    new PollingConditions(timeout: 5).eventually {
      items.size() == concurrentSubs
    }

    cleanup:
    muon.shutdown()
    Thread.sleep(2000)
  }

  def "Multiple concurrent muon based subscribers can open the same channel"() {

    def muon = new Muon(new AmqpDiscovery("amqp://localhost"))
    muon.serviceIdentifer = "eventsource"
    new AmqpTransportExtension("amqp://localhost").extend(muon)
    def pub = Broadcaster.create()
    muon.start()

    muon.streamSource("/core", Map, pub)

    Thread.sleep(3500)

    def items = []
    int concurrentSubs = 10

    Executors.newCachedThreadPool()

    concurrentSubs.times {
      Thread.start {
        def stream = Broadcaster.create()

        stream.observeError(Exception){
          println "Error!"
        }.consume {
          items << it
        }
        muon.subscribe("muon://eventsource/core", Map, stream)
      }
    }

    Thread.sleep(12000)

    when:
    pub.accept(["message":"is awesome"])

    then:
    new PollingConditions(timeout: 5).eventually {
      items.size() == concurrentSubs
    }

    cleanup:
    muon.shutdown()
  }
}

