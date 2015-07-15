package io.muoncore.stream

import io.muoncore.Muon
import io.muoncore.extension.amqp.AmqpTransportExtension
import io.muoncore.extension.amqp.discovery.AmqpDiscovery
import reactor.rx.broadcast.Broadcaster
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class MultiDistinctSubscribeLoadedSpec extends Specification {

  def "A loaded channel remains available."() {

    given:
    def muon = new Muon(new AmqpDiscovery("amqp://localhost"))
    muon.serviceIdentifer = "eventsource"
    new AmqpTransportExtension("amqp://localhost").extend(muon)

    def pub = Broadcaster.create()
        .capacity(5000)

    def consumes = []
    pub.consume {
      consumes << it
    }

    muon.start()

    muon.streamSource("/core", Map, pub)

    int messages = 5000
    def numSubscribers = 20
    def MuonResourceService = []
    def items = []

    def clientMuons = []

    numSubscribers.times {
      def localitems = []
      items << localitems
      def stream = Broadcaster.create()

      stream.observeError(Exception) {
        println "Error!"
      }.consume {
          localitems << it
      }

      muon.subscribe("muon://eventsource/core", Map, stream)
    }



    Thread.sleep(3500)


    and:
    Thread.sleep(6000)

    when:
    def cl = 0
    messages.times {
      if(++cl % 100 == 0) println "$cl"
      pub.accept(["message":"is awesome"])
    }

    then:
    new PollingConditions(timeout: 10).eventually {
      def size = items*.size()
      def consumessize = consumes.size()
      consumessize == messages
      items*.size().sum() == messages * numSubscribers
    }

    cleanup:
    muon.shutdown()
  }
}

