package io.muoncore.protocol

import io.muoncore.Discovery
import io.muoncore.Muon
import io.muoncore.channel.Channels
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.MuonConfigBuilder
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonOutboundMessage
import io.muoncore.transport.client.TransportClient
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.function.Consumer
import java.util.function.Function

class ServerJSProtocolSpec extends Specification {

  Muon muon
  TransportClient client

  def setup() {
    def disco = Mock(Discovery) {
      getCodecsForService(_) >> ["application/json"]
    }
    client = Mock(TransportClient)
    muon = Mock(Muon) {
      getConfiguration() >> MuonConfigBuilder.withServiceIdentifier("my-service").build()
      getTransportClient() >> client
      getDiscovery() >> disco
      getCodecs() >> new JsonOnlyCodecs()
    }
  }

  def "protocol can access state set by the stack"() {
    def transportchannel = Channels.channel("left", "right")

    MuonOutboundMessage ret

    transportchannel.left().receive {
      ret = it
    }

    when:
    def proto = new ServerJSProtocol(
      muon, "faked", transportchannel.right()
    )
    proto.setState("mystate", ["hello":"world"])
    proto.start(JS_ACCESS_STATE)

    transportchannel.left().send(MuonMessageBuilder.fromService("awesome").step("hello").buildInbound())

    then:
    new PollingConditions().eventually {
      ret instanceof MuonOutboundMessage
      def decoded = muon.getCodecs().decode(ret.payload, ret.contentType, Map)
      decoded.hello == "world"
    }
  }

  def "protocol can invoke functions in the state set by the stack"() {
    def transportchannel = Channels.channel("left", "right")

    MuonOutboundMessage ret

    transportchannel.left().receive {
      ret = it
    }

    when:
    def proto = new ServerJSProtocol(
      muon, "faked", transportchannel.right()
    )
    proto.setState("mystate", new Function() {
      @Override
      Object apply(Object o) {
        println "Being invoked"
        return [
                "hello": "world",
          message: o
        ]
      }
    })

    proto.start(JS_INVOKE_STATE)

    transportchannel.left().send(MuonMessageBuilder.fromService("awesome").step("hello").buildInbound())

    then:
    new PollingConditions().eventually {
      ret instanceof MuonOutboundMessage
      def decoded = muon.getCodecs().decode(ret.payload, ret.contentType, Map)
      decoded.hello == "world"
      decoded.message == "mymessage"
    }
  }

  def "protocol can provide functions for later invocation by the stack"() {
    def transportchannel = Channels.channel("left", "right")

    MuonOutboundMessage ret

    transportchannel.left().receive {
      ret = it
    }

    when:
    def proto = new ServerJSProtocol(
      muon, "faked", transportchannel.right()
    )
    proto.setState("callmelater", new Consumer<Consumer>() {
      @Override
      void accept(Consumer o) {
        println "CALLING THE RUNNABLE $o"
        o.accept([hello: "world"])
      }
    })

    proto.start(JS_INVOKE_LATER)

    transportchannel.left().send(MuonMessageBuilder.fromService("awesome").step("hello").buildInbound())

    then:
    new PollingConditions().eventually {
      ret instanceof MuonOutboundMessage
      def decoded = muon.getCodecs().decode(ret.payload, ret.contentType, Map)
      decoded.hello == "world"
    }
  }


  static JS_ACCESS_STATE = """
module.exports = function(api) {
  return {
    fromTransport: function(msg) {
      api.sendTransport({
        payload: api.state("mystate")
      })
    }
  }  
}"""

  static JS_INVOKE_STATE = """
module.exports = function(api) {
  return {
    fromTransport: function(msg) {
      api.sendTransport({
        payload: api.state("mystate")("mymessage")
      })
    }
  }  
}"""

  static JS_INVOKE_LATER = """
module.exports = function(api) {
  return {
    fromTransport: function(msg) {
      api.state("callmelater")(function(arg) {
        api.sendTransport({
          payload: arg
        })
      })
    }
  }  
}"""
}
