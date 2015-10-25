package io.muoncore.future

import io.muoncore.future.MuonFuture
import io.muoncore.future.PublisherBackedFuture
import reactor.rx.Streams
import reactor.rx.broadcast.Broadcaster
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class PublisherBackedFutureSpec extends Specification {

  def "promise can be used in a stream"() {
    given:
    Broadcaster<Map> stream = Broadcaster.create()
    MuonFuture<Map> promise = new PublisherBackedFuture<Map>(stream)

    when:

    def captured

    Streams.wrap(promise.toPublisher()).consume {
      captured = it
    }

    stream.accept([:])

    then:
    new PollingConditions(timeout: 2).eventually {
      captured == [:]
    }
  }

  def "promise can do sync return when item is generated after get() is called"() {
    given:
    Broadcaster<Map> stream = Broadcaster.create()
    MuonFuture<Map> promise = new PublisherBackedFuture<Map>(stream)

    when:

    Thread.start {
      Thread.sleep(200)
      stream.accept([:])
    }
    def captured = promise.get()

    then:
    captured == [:]
  }
}
