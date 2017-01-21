package io.muoncore.channel

import io.muoncore.channel.impl.ZipChannel
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonOutboundMessage
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class ZipChannelSpec extends Specification {

  def "will inflate an inbound muon message"() {
    def channel = Channels.zipChannel("zip")

    MuonInboundMessage inflated

    channel.left().receive({
      inflated = it
    })

    when:
    channel.right().send(MuonMessageBuilder
      .fromService("service1")
      .payload(ZipChannel.zlibDeflate("Hello World".getBytes()))
      .contentType("application/json+DEFLATE")
      .buildInbound()
    )

    then:
    new PollingConditions().eventually {
      inflated && inflated.contentType == "application/json" && new String(inflated.payload) == "Hello World"
    }
  }

  def "will deflate an outbound muon message"() {
    def channel = Channels.zipChannel("zip")

    MuonOutboundMessage deflated

    channel.right().receive({
      deflated = it
    })

    when:
    channel.left().send(MuonMessageBuilder
      .fromService("service1")
      .payload("Hello World".getBytes())
      .contentType("application/json")
      .build()
    )

    then:
    new PollingConditions().eventually {
      deflated && deflated.contentType == "application/json+DEFLATE" && new String(ZipChannel.zlibInflate(deflated.payload)) == "Hello World"
    }
  }

  def "performance is adequate"() {

    def iterations = 100000
    def targetmsPerDeflation = 0.1

    def channel = Channels.zipChannel("zip")

    def deflated = []

    channel.right().receive({
      deflated << it
    })

    when:
    def then = System.currentTimeMillis()
    iterations.times {
      channel.left().send(MuonMessageBuilder
        .fromService("service1")
        .payload("Hello World".getBytes())
        .contentType("application/json")
        .build()
      )
    }

    then:
    new PollingConditions().eventually {
      deflated.size() == iterations
    }
    def now = System.currentTimeMillis()

    def totalTime = now - then

    println "Took ${totalTime}ms = ${totalTime / iterations}ms per item"
    totalTime < iterations * targetmsPerDeflation
  }
}
