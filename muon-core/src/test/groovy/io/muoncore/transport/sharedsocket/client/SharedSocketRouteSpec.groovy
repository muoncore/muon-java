package io.muoncore.transport.sharedsocket.client

import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.protocol.ChannelFunctionExecShimBecauseGroovyCantCallLambda
import io.muoncore.transport.TransportEvents
import io.muoncore.transport.client.TransportConnectionProvider
import spock.lang.Specification

class SharedSocketRouteSpec extends Specification {


  def "when a transport channel fails, SharedSocketRoute notifies of failure"() {
    def channel = Mock(ChannelConnection)
    def function

    def onfail = Mock(Runnable)

    TransportConnectionProvider connectionProvider = Mock(TransportConnectionProvider) {
      connectChannel(_,_,_,) >> { args ->
        function = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(args[2])
        return channel
      }
    }

    def route = new SharedSocketRoute("service", connectionProvider, new JsonOnlyCodecs(), new AutoConfiguration(), onfail)

    when: "the transport connection reports failure"
    function(MuonMessageBuilder.fromService("hello-world")
      .step(TransportEvents.CONNECTION_FAILURE)
      .operation(MuonMessage.ChannelOperation.closed)
      .contentType("text/plain")
      .payload(new byte[0])
      .buildInbound())

    then: "The SharedSocketRoute reports failure"

    1 * onfail.run()
  }

  def "when the transport channel sends error, all virt channels are closed"() {

    def channel = Mock(ChannelConnection)
    def inboundFunc = Mock(ChannelConnection.ChannelFunction)
    def function

    TransportConnectionProvider connectionProvider = Mock(TransportConnectionProvider) {
      connectChannel(_,_,_,) >>  { args ->
        function = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(args[2])
        return channel
      }
    }

    def channels = []

    def route = new SharedSocketRoute("service", connectionProvider, new JsonOnlyCodecs(), new AutoConfiguration(), {
      println "Shutting down"
    })

    when: "open channel connections"
    channels << route.openClientChannel()
    channels << route.openClientChannel()
    channels << route.openClientChannel()
    channels.each { it.receive(inboundFunc) }

    and: "the transport connection reports failure"

    function(MuonMessageBuilder.fromService("hello-world")
      .step(TransportEvents.CONNECTION_FAILURE)
      .operation(MuonMessage.ChannelOperation.closed)
      .contentType("text/plain")
      .payload(new byte[0])
      .buildInbound())

    then: "All channel connections are notified of ChannelFailure"
    3 * inboundFunc.apply({
      it.channelOperation == MuonMessage.ChannelOperation.closed
    })

  }
}
