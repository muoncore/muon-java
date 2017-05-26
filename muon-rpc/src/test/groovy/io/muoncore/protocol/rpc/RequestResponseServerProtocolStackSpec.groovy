package io.muoncore.protocol.rpc

import io.muoncore.Discovery
import io.muoncore.Muon
import io.muoncore.ServiceDescriptor
import io.muoncore.codec.json.GsonCodec
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.config.MuonConfigBuilder
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.protocol.rpc.server.HandlerPredicates
import io.muoncore.protocol.rpc.server.RequestResponseHandlers
import io.muoncore.protocol.rpc.server.RequestResponseServerHandler
import io.muoncore.protocol.rpc.server.RequestResponseServerProtocolStack
import io.muoncore.protocol.rpc.server.RequestWrapper
import io.muoncore.protocol.rpc.server.ServerResponse
import io.muoncore.transport.client.TransportClient
import reactor.Environment
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class RequestResponseServerProtocolStackSpec extends Specification {

  Muon muon
  TransportClient client

  def setup() {
    def disco = Mock(Discovery) {
      getCodecsForService(_) >> ["application/json"]
      getServiceNamed(_) >> Optional.of(new ServiceDescriptor("tombola", [], ["application/json+AES"], [], []))
    }
    client = Mock(TransportClient)
    muon = Mock(Muon) {
      getConfiguration() >> new AutoConfiguration(serviceName: "tombola")
      getDiscovery() >> disco
      getCodecs() >> new JsonOnlyCodecs()
    }
  }

  def "handler can be invoked via the external channel"() {
    Environment.initializeIfEmpty()
    def handler = Mock(RequestResponseServerHandler) {
      getRequestType() >> Map
    }
    def handlers = Mock(RequestResponseHandlers) {
      findHandler(_) >> handler

    }
    def stack = new RequestResponseServerProtocolStack(handlers, muon)

    when:
    def channel = stack.createChannel()
    channel.send(inbound("123", "FAKESERVICE", "requestresponse"))
    Thread.sleep(50)

    then:
    1 * handler.handle(_)
  }

  def "handler can reply down the channel"() {
    Environment.initializeIfEmpty()
    def handler = Mock(RequestResponseServerHandler) {
      handle(_) >> { RequestWrapper wrapper ->
        wrapper.answer(new ServerResponse(200, "hello"))
      }
      getRequestType() >> Map
    }

    def handlers = Mock(RequestResponseHandlers) {
      findHandler(_) >> handler
    }
    def stack = new RequestResponseServerProtocolStack(handlers, muon)

    def responseReceived

    when:
    def channel = stack.createChannel()
    channel.receive({
      if (it) responseReceived = it
    })

    channel.send(inbound("123", "FAKESERVICE", "requestresponse"))
    Thread.sleep(50)

    then:
    new PollingConditions().eventually {
      responseReceived != null
    }
  }

  def "returns protocol descriptor"() {
    def handler = Mock(RequestResponseServerHandler) {
      handle(_) >> { RequestWrapper wrapper ->
        wrapper.answer(new ServerResponse(200, "hello"))
      }
      getRequestType() >> Map
    }

    def handlers = Mock(RequestResponseHandlers) {
      findHandler(_) >> handler
      getHandlers() >> [
        mockHandler(),
        mockHandler(),
        mockHandler(),
      ]
    }
    def stack = new RequestResponseServerProtocolStack(handlers, muon)

    when:
    def protocolDescriptor = stack.protocolDescriptor

    then:
    protocolDescriptor.protocolScheme == "rpc"
    protocolDescriptor.operations.size() == 3

  }

  def "returns schema descriptors"() {
    def handler = Mock(RequestResponseServerHandler) {
      handle(_) >> { RequestWrapper wrapper ->
        wrapper.answer(new ServerResponse(200, "hello"))
      }
      getRequestType() >> Map
    }

    def handlers = Mock(RequestResponseHandlers) {
      findHandler(_) >> handler
      getHandlers() >> [
        mockHandler(),
        mockHandler(),
        mockHandler(),
      ]
    }
    def stack = new RequestResponseServerProtocolStack(handlers, muon)

    when:
    def protocolDescriptor = stack.protocolDescriptor

    then:
    protocolDescriptor.protocolScheme == "rpc"
    protocolDescriptor.operations.size() == 3

  }

  def mockHandler() {
    Mock(RequestResponseServerHandler) {
      getPredicate() >> HandlerPredicates.path("simples")
    }
  }

  def inbound(id, String service, String protocol) {
    MuonMessageBuilder.fromService("localService")
      .toService(service)
      .step("request.made")
      .protocol(protocol)
      .contentType("application/json")
      .payload(new GsonCodec().encode(new Request(new URI("request://myurl"), [:])))
      .buildInbound()
  }
}


