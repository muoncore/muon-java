package io.muoncore.transport.sharedsocket.server

import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.protocol.ServerStacks
import io.muoncore.transport.TransportEvents
import io.muoncore.transport.sharedsocket.client.messages.SharedChannelOutboundMessage
import spock.lang.Specification

class SharedSocketServerChannelSpec extends Specification {

  static def codecs = new JsonOnlyCodecs()

  def "server channel forwards virtual channel shutdown"() {

    def server = Mock(ChannelConnection)

    def stacks = Mock(ServerStacks) {

    }

    def channel = new SharedSocketServerChannel(stacks, codecs)
    when:
    channel.send(sharedMessage(newConnection("321")))
    channel.send(sharedMessage(closeVirtualProtocol("321")))
    channel.send(sharedMessage(newConnection("321")))

    then: "server is inferred to be closed and removed, therefore a duplicate channel ID will cause a new channel to ope"
    2 * stacks.openServerChannel("epic") >> server
  }

  def "server channel propagates transport failure"() {

    def server = Mock(ChannelConnection)

    def stacks = Mock(ServerStacks) {
      openServerChannel("epic") >> server
    }

    def channel = new SharedSocketServerChannel(stacks, codecs)

    channel.send(sharedMessage(newConnection("123")))
    channel.send(sharedMessage(newConnection("321")))

    when:
    channel.send(transportFailure())

    then:
    2 * server.shutdown()
  }

  MuonInboundMessage sharedMessage(SharedChannelOutboundMessage msg) {
    MuonMessageBuilder.fromService("awesome")
      .toService("faked")
      .step(SharedChannelServerStacks.STEP)
      .payload(codecs.encode(msg, ["application/json"] as String[]).payload)
      .contentType("application/json")
      .buildInbound()
  }

  SharedChannelOutboundMessage newConnection(def channelId) {
    new SharedChannelOutboundMessage(channelId,
      MuonMessageBuilder.fromService("awesome")
        .toService("faked")
        .protocol("epic")
        .step("awesome")
        .build()
    )
  }

  SharedChannelOutboundMessage closeVirtualProtocol(def channelId) {
    new SharedChannelOutboundMessage(channelId,
      MuonMessageBuilder.fromService("awesome")
        .toService("faked")
        .protocol("epic")
        .step("awesome")
        .operation(MuonMessage.ChannelOperation.closed)
        .build()
    )
  }

  MuonInboundMessage transportFailure() {
    MuonMessageBuilder.fromService("awesome")
        .toService("faked")
        .step(TransportEvents.CONNECTION_FAILURE)
        .operation(MuonMessage.ChannelOperation.closed)
        .buildInbound()
  }
}
