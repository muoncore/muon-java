package io.muoncore.inmem.transport

import io.muoncore.Discovery
import io.muoncore.channel.support.Scheduler
import io.muoncore.codec.Codecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.transport.InMemClientChannelConnection
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.memory.transport.OpenChannelEvent
import io.muoncore.memory.transport.bus.EventBus
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.protocol.ServerStacks
import io.muoncore.transport.TransportEvents
import io.muoncore.transport.TransportFailureSpec
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class InMemTransportSpec extends Specification {

  Codecs codecs = Mock(Codecs) {
    encode(_, _) >> new Codecs.EncodingResult(new byte[0], "application/json")
  }

  def "returns client channel connection on demand."() {
    def eventbus = new EventBus()
    def serverStacks = Mock(ServerStacks)

    def transport = new InMemTransport(new AutoConfiguration(serviceName: "tombola"), eventbus)
    transport.start(Mock(Discovery), serverStacks, codecs, new Scheduler())

    when:
    def ret = transport.openClientChannel("tombola", "simple")

    then:
    ret instanceof InMemClientChannelConnection
  }

  def "transport listens on event bus for OpenChannelEvents. opens channels in response"() {
    def eventbus = new EventBus()
    def serverStacks = Mock(ServerStacks)
    def clientConnection = Mock(InMemClientChannelConnection)

    def transport = new InMemTransport(new AutoConfiguration(serviceName: "tombola"), eventbus)
    transport.start(Mock(Discovery), serverStacks, codecs, new Scheduler())

    when:
    eventbus.post(new OpenChannelEvent(
      "tombola", "simple", clientConnection))

    then:
    1 * serverStacks.openServerChannel("simple")
  }

  def "when failurs is triggered, channel connections are immediately closed with a transport failure error"() {
    def eventbus = new EventBus()
    def serverStacks = Mock(ServerStacks)

    def transport = new InMemTransport(new AutoConfiguration(serviceName: "tombola"), eventbus)
    transport.start(Mock(Discovery), serverStacks, codecs, new Scheduler())
    transport.triggerFailure()

    when:
    def ret = transport.openClientChannel("tombola", "simple")

    MuonInboundMessage fail

    ret.receive {
      fail = it
    }

    ret.send(MuonMessageBuilder.fromService("me").build())

    then:
    new PollingConditions().eventually {
      fail
      fail.step == TransportEvents.CONNECTION_FAILURE
    }
  }
}
