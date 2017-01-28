package io.muoncore.simulation

import com.google.common.eventbus.EventBus
import io.muoncore.MultiTransportMuon
import io.muoncore.Muon
import io.muoncore.channel.impl.StandardAsyncChannel
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.protocol.reactivestream.messages.ReactiveStreamSubscriptionRequest
import io.muoncore.protocol.reactivestream.server.PublisherLookup
import io.muoncore.protocol.reactivestream.server.ReactiveStreamServerHandlerApi
import io.muoncore.protocol.requestresponse.Response
import io.muoncore.protocol.requestresponse.server.ServerResponse
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import reactor.Environment
import reactor.rx.broadcast.Broadcaster
import spock.lang.Specification
import spock.lang.Timeout
import spock.util.concurrent.PollingConditions

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all

@Timeout(10)
class MultiProtocolSimulationSpec extends Specification {

  def "overlapping multiple protocols works"() {
    def eventbus = new EventBus()
    Environment.initializeIfEmpty()

    given: "some services"

    def discovery = new InMemDiscovery()

    def service1 = createService("hello", discovery, eventbus)
    def service2 = createService("world", discovery, eventbus)

    service1.handleRequest(all()) {
      it.ok([svc: "svc1"])
    }

    def streams = []

    service1.publishGeneratedSource("data", PublisherLookup.PublisherType.HOT) {
      ReactiveStreamSubscriptionRequest subscriptionRequest ->
        println "ACTIVATING"
        Broadcaster b = Broadcaster.create()
        streams << b
        return b
    }

    def datas = []
    def streamdatas = []

    discovery.blockUntilReady()

    when:
    datas << service1.request("rpc://hello/mydata").get()
    service1.subscribe(new URI("stream://hello/data"), String, new Subscriber() {
      @Override
      void onSubscribe(Subscription s) {
        s.request(Integer.MAX_VALUE)
      }

      @Override
      void onNext(Object o) { streamdatas << o }

      @Override
      void onError(Throwable t) {t.printStackTrace()}

      @Override
      void onComplete() {}
    })

    datas << service1.request("rpc://hello/mydata").get()
    service1.subscribe(new URI("stream://hello/data"), String, new Subscriber() {
      @Override
      void onSubscribe(Subscription s) {
        s.request(Integer.MAX_VALUE)
      }

      @Override
      void onNext(Object o) { streamdatas << o }

      @Override
      void onError(Throwable t) { t.printStackTrace()}

      @Override
      void onComplete() {}
    })

    streams.each {
      println "SENDING"
      it.accept("SOME DATA")
    }

    then:
    new PollingConditions().eventually {
      streamdatas.size() == 2
    }

    cleanup:
    service1*.shutdown()
    service2*.shutdown()
  }

  Muon createService(ident, discovery, eventbus) {
    def config = new AutoConfiguration(serviceName: ident)
    def transport = new InMemTransport(config, eventbus)

    new MultiTransportMuon(config, discovery, [transport])
  }
}
