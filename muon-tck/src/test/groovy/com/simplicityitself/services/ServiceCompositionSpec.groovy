package com.simplicityitself.services

import io.muoncore.Muon
import io.muoncore.MuonClient
import io.muoncore.future.MuonFutures
import io.muoncore.extension.amqp.AmqpTransportExtension
import io.muoncore.extension.amqp.discovery.AmqpDiscovery
import reactor.Environment
import reactor.fn.Consumer
import reactor.fn.Function
import reactor.rx.Stream
import reactor.rx.Streams
import reactor.rx.broadcast.Broadcaster
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.TimeUnit

class ServiceCompositionSpec extends Specification {

  @AutoCleanup("shutdown")
  Muon muon = muon()

  def "can compose multiple services with failover"() {

    given:
    Environment.initializeIfEmpty()

    slowResource()
    quickResource()

    def resources

    when:

    muon.get("muon://muonservice/fast", String).get()
    Stream<String> s2 = callResourceWithTimeout("fast")
    Stream<String> s1 = callResourceWithTimeout("slow")

    Stream<List<String>> aggregate = Streams.join(s1, s2);

    aggregate.log("aggregate").consume {
        println "Got " + it
        resources = it
    }

    then:

    new PollingConditions(timeout: 5).eventually {
      resources == [ "This is fast!", "This is the default slow message" ]
    }
  }

  private Stream<String> callResourceWithTimeout(resource) {
    return Streams.wrap(
        muon.get("muon://muonservice/${resource}", String).toPublisher())
        .map {
        return it.responseEvent.decodedContent;
    }.timeout(3000, TimeUnit.MILLISECONDS,
        Streams.just("This is the default slow message")).log("res-${resource}");
  }

  private quickResource() {
    muon.onGet("/quick", Map) {
      println "Called /quick"
      return MuonFutures.immediately("This is fast!")
    }
  }

  private slowResource() {
    muon.onGet("/slow", Map) {
      println "Called /slow"
      def pub = Broadcaster.create()
      Thread.start {
        Thread.sleep(2000)
        pub.accept("My message")
      }

      return MuonFutures.fromPublisher(pub)
    }
  }

  def muon() {
    def discovery1 = new AmqpDiscovery("amqp://localhost")
    def muon = new Muon(discovery1)
    muon.serviceIdentifer = "muonservice"
    new AmqpTransportExtension("amqp://localhost").extend(muon)
    muon.start()
    Thread.sleep(3500)
    muon
  }
}
