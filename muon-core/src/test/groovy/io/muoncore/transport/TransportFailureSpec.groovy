package io.muoncore.transport

import com.google.common.eventbus.EventBus
import io.muoncore.MultiTransportMuon
import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.json.GsonCodec
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.descriptors.ProtocolDescriptor
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.protocol.ChannelFunctionExecShimBecauseGroovyCantCallLambda
import io.muoncore.protocol.ServerProtocolStack
import spock.lang.Specification

class TransportFailureSpec extends Specification {

  def eventbus = new EventBus()

  def clienttransport

  def discovery = new InMemDiscovery()

  def "on transport failure, sends shutdown to all channels" () {

    def sendToClient

    ChannelConnection connection = Mock(ChannelConnection) {
      receive(_) >> {
        sendToClient = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(it[0])
      }
    }

    def protocol = Mock(ServerProtocolStack) {
      createChannel() >> connection
      getProtocolDescriptor() >> new ProtocolDescriptor("rpc", "rpc", "hello", [])
    }

    def (client, clienttransport) = createService("client", discovery)
    def (server, nothing) = createService("server", discovery)

    server.protocolStacks.registerServerProtocol(protocol)

    def failure = Mock(ChannelConnection.ChannelFunction)

    when: "multiple channels established between muons"

    def channel = client.transportClient.openClientChannel()
    def channel2 = client.transportClient.openClientChannel()

    [channel, channel2].each {
      it.receive(failure)
      it.send(outbound("server", "rpc"))
    }

    and: "Transport fails"

    sleep(100)
    clienttransport.triggerFailure()
    sleep(100)

    then: "All channels recieve ChannelFailure and shutdown"
    2 * failure.apply(_)
  }

  def createService(ident, discovery) {
    def config = new AutoConfiguration(serviceName: "${ident}")
    def transport = new InMemTransport(config, eventbus)

    [new MultiTransportMuon(config, discovery, [transport], new JsonOnlyCodecs()), transport]
  }

  def outbound(String service, String protocol) {
    MuonMessageBuilder
      .fromService("localService")
      .toService(service)
      .step("somethingHappened")
      .protocol(protocol)
      .contentType("application/json")
      .payload(new GsonCodec().encode([:]))
      .build()
  }
}
