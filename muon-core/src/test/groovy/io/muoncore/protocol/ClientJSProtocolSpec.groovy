package io.muoncore.protocol

import io.muoncore.Discovery
import io.muoncore.Muon
import io.muoncore.channel.ChannelConnection
import io.muoncore.channel.Channels
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.MuonConfigBuilder
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonOutboundMessage
import io.muoncore.messages.MuonMessage
import io.muoncore.transport.client.TransportClient
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class ClientClientJSProtocolSpec extends Specification {

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

  def "receive messages left onstandard handler"() {

    client.openClientChannel() >> Mock(ChannelConnection)

    def channel = Channels.channel("left", "right")

    def proto = new ClientJSProtocol(
      muon, "faked", channel.right()
    )

    proto.start(JS_RECIEVE)

    def ret = null

    channel.left().receive {
      ret = it
    }

    when:
    channel.left().send("HELLO WORLD")

    then:
    new PollingConditions().eventually {
      ret
    }
  }

  def "can access muon configuration"() {

    client.openClientChannel() >> Mock(ChannelConnection)
    def channel = Channels.channel("left", "right")

    def proto = new ClientJSProtocol(
      muon, "faked", channel.right()
    )

    proto.start(JS_CONFIG)

    def ret
    channel.left().receive {
      ret = it
    }

    when:
    channel.left().send("HELLO WORLD")

    then:
    new PollingConditions().eventually {
      ret == "my-service"
    }
  }

  def "open channels from the tsclient"() {

    def channel = Channels.channel("left", "right")
    def transportchannel = Channels.channel("left", "right")
    def transportdata = []
    def ret = null

    channel.left().receive {
      ret = it
    }

    transportchannel.right().receive {
      transportdata << it
    }

    when:
    def proto = new ClientJSProtocol(
      muon, "faked", channel.right()
    )
    proto.start(JS_SENDTRANSPORT)

    channel.left().send("HELLO WORLD")

    Thread.sleep(200)

    then:
    1 * client.openClientChannel() >> transportchannel.left()
    transportdata.size() == 2
  }

  def "receive messages from the right and process in handler"() {

    def channel = Channels.channel("left", "right")
    def transportchannel = Channels.channel("left", "right")
    def ret = []

    channel.left().receive {
      ret << it
    }

    when:
    def proto = new ClientJSProtocol(
      muon, "faked", channel.right()
    )
    proto.start(JS_RECTRANSPORT)

    transportchannel.right().send("HELLO WORLD")
    transportchannel.right().send("HELLO WORLD")
    transportchannel.right().send("HELLO WORLD")

    Thread.sleep(200)

    then:
    1 * client.openClientChannel() >> transportchannel.left()
    ret.size() == 1
    ret[0] == 3
  }

  def "use timer to schedule an action/ message send"() {

    def channel = Channels.channel("left", "right")
    def transportchannel = Channels.channel("left", "right")
    client.openClientChannel() >> transportchannel.left()
    def ret = []

    channel.left().receive {
      ret << it
    }

    when:
    def proto = new ClientJSProtocol(
      muon, "faked", channel.right()
    )
    proto.start(JS_TIMED_MESSAGE_API)

    channel.left().send("Hello")

    then:
    ret.size() == 0

    and: "data arrives later"
    new PollingConditions().eventually {
      ret.size() == 1
    }
  }

  def "cancel timed action"() {

    def channel = Channels.channel("left", "right")
    def transportchannel = Channels.channel("left", "right")
    client.openClientChannel() >> transportchannel.left()
    def ret = []

    channel.left().receive {
      ret << it
    }

    when:
    def proto = new ClientJSProtocol(
      muon, "faked", channel.right()
    )
    proto.start(JS_TIMED_MESSAGE_CANCEL)

    channel.left().send("Hello")

    then:
    ret.size() == 0

    and: "data doesn't arrive later"
    sleep(300)
    ret.size() == 0
  }

  def "shutdown on demand"() {

    def channel = Channels.channel("left", "right")
    def transportchannel = Channels.channel("left", "right")
    client.openClientChannel() >> transportchannel.left()
    def ret = []

    channel.left().receive {
      println "GOT API DATA $it"
      ret << it
    }
    transportchannel.right().receive {
      println "GOT TRANSPORT RIGHT $it"
      ret << it
    }

    when:
    def proto = new ClientJSProtocol(
      muon, "faked", channel.right()
    )
    proto.start(JS_SHUTDOWN)

    channel.left().send("Hello")

    then: "See the null messages used to poison/ shut down the two channels"
    new PollingConditions().eventually {
      ret == [null, null]
    }
  }

  def "enforce muon message schemas sending right"() {

    def channel = Channels.channel("left", "right")
    def transportchannel = Channels.channel("left", "right")
    client.openClientChannel() >> transportchannel.left()

    MuonOutboundMessage ret

    channel.left().receive {
    }

    transportchannel.right().receive {
      ret = it
    }

    when:
    def proto = new ClientJSProtocol(
      muon, "faked", channel.right()
    )
    proto.start(JS_TRANSPORT_MUON_MESSAGE)

    channel.left().send("Hello")

    then:
    new PollingConditions().eventually {
      ret
      ret.step == "cool"
      ret.targetServiceName == "awesome"
      ret.protocol == "faked"
    }
  }

  def "enforce provided schema going left and convert to correct concrete type for API"() {

    def channel = Channels.channel("left", "right")
    def transportchannel = Channels.channel("left", "right")
    client.openClientChannel() >> transportchannel.left()
    def ret

    channel.left().receive {
      ret = it
    }
    transportchannel.right().receive {
    }

    when:
    def proto = new ClientJSProtocol(
      muon, "faked", channel.right()
    )
    proto.addTypeForCoercion("MyResponseType", { new MyResponseType(name: it.name) })
    proto.start(JS_API_MUON_MESSAGE)

    transportchannel.right().send(
      MuonMessageBuilder.fromService("mine").step("awesome").buildInbound())

    then:
    new PollingConditions().eventually {
      ret
      ret instanceof MyResponseType
      ret.name == "cool"
    }
  }

  def "can decode message payloads from the transport"() {

    def channel = Channels.channel("left", "right")
    def transportchannel = Channels.channel("left", "right")
    client.openClientChannel() >> transportchannel.left()
    MyResponseType ret

    channel.left().receive {
      ret = it
    }
    transportchannel.right().receive {}


    when:
    def proto = new ClientJSProtocol(
      muon, "faked", channel.right()
    )
    proto.addTypeForCoercion("MyResponseType", { new MyResponseType(name: it.name, payload: it.payload) })
    proto.addTypeForDecoding("MyResponseTypeWrapped", MyResponseTypeWrapped)
    proto.start(JS_API_DECODE)

    def payload = new JsonOnlyCodecs().encode(["name":"world"], new JsonOnlyCodecs().getAvailableCodecs())

    transportchannel.right().send(
      MuonMessageBuilder.fromService("mine").payload(payload.payload).contentType(payload.contentType).step("awesome").buildInbound())

    then:
    new PollingConditions().eventually {
      ret instanceof MyResponseType
      ret.payload instanceof MyResponseTypeWrapped
      ret.payload.name == "world"
    }
  }

  def "can encode message payloads for sending to the transport"() {

    def channel = Channels.channel("left", "right")
    def transportchannel = Channels.channel("left", "right")
    client.openClientChannel() >> transportchannel.left()
    MuonOutboundMessage ret

    channel.left().receive {}

    transportchannel.right().receive {
      ret = it
    }

    when:
    def proto = new ClientJSProtocol(
      muon, "faked", channel.right()
    )
    proto.start(JS_API_ENCODE)

    channel.left().send(new MyResponseType(name: "HELLO!", payload: ["hello":"AWESOME"]))

    then:
    new PollingConditions().eventually {
      ret instanceof MuonOutboundMessage
      println new String(ret.payload)
      ret.targetServiceName == "target-service"
      def decoded = muon.getCodecs().decode(ret.payload, ret.contentType, MyResponseType)
      decoded.name == "HELLO!"
      decoded.payload.hello == "AWESOME"
    }
  }

  static JS_RECIEVE = """
module.exports = function(api) {
  return {
    fromApi: function(msg) {
       api.sendApi("YO, DUDE")
    }
   }
}"""

  static JS_CONFIG = """
module.exports = function(api) {
  return {
    fromApi: function(msg) {
  print("CALLING SERVICE " + api.serviceName())
  api.sendApi(api.serviceName())
  }
  }
}"""

  static JS_SENDTRANSPORT = """
module.exports = function(api) {
  return {
 fromApi: function(msg) {
  print("CALLING SERVICE " + api.serviceName())
  api.sendTransport({"hello":"SIMPLES"})
  api.sendTransport({"hello":"SIMPLES"})
  }
  }
}"""
  static JS_RECTRANSPORT = """
module.exports = function(api) {
var transportmessages = []
  return {
    fromApi: function(msg) {
  
    },
    fromTransport: function(msg) {
      transportmessages.push(msg)
      if (transportmessages.length == 3) {
        api.sendApi(3)
      }
    }
  }
}"""

  static JS_TIMED_MESSAGE_API = """
module.exports = function(api) {
  return {
    fromApi: function(msg) {
      setTimeout(function() { 
        api.sendApi("YO")
      }, 100);
    },
    fromTransport: function(msg) {
    }
  }
}"""

  static JS_TIMED_MESSAGE_CANCEL = """
module.exports = function(api) {
  return {
    fromApi: function(msg) {
      var handle = setTimeout(function() { 
         api.sendApi("YO")
      }, 100);
      clearTimeout(handle)
    },
    fromTransport: function(msg) {
    
    }
  }
}"""

  static JS_SHUTDOWN = """
module.exports = function(api) {
  return {
    fromApi: function(msg) {
      log.info("Sending shutdown")
      api.shutdown()
    },
    fromTransport: function(msg) {
    }
  }
}"""
  static JS_TRANSPORT_MUON_MESSAGE = """
module.exports = function(api) {
  return {
    fromApi: function(msg) {
      api.sendTransport({
        payload: {
        },
        target:"awesome",
        step: "cool"
      })
    },
    fromTransport: function(msg) {
    
    }
  }
}"""
  static JS_API_MUON_MESSAGE = """
module.exports = function(api) {
  return {
    fromApi: function(msg) {
      
    },
    fromTransport: function(msg) {
    api.sendApi(api.type("MyResponseType", {
        name: "cool",
        value: "simple"
      }))
    }
  }
}"""

  static JS_API_DECODE = """
module.exports = function(api) {
  return {
    fromApi: function(msg) {
    },
    fromTransport: function(msg) {
      var resp = api.decode("MyResponseTypeWrapped", msg)
      api.sendApi(api.type("MyResponseType", {
        name: "Hello",
        payload: resp
      }))
    }
  }
}"""

  static JS_API_ENCODE = """
module.exports = function(api) {
  return {
    fromApi: function(msg) {
      var resp = api.encodeFor(msg, "myservice")
      api.sendTransport({
        target: "target-service",
        step: 'hello',
        payload: resp.payload,
        contentType: resp.contentType
      })
    },
    fromTransport: function(msg) {
    }
  }  
}"""
}

class MyResponseTypeWrapped {
  String name
  String value
}

class MyResponseType {
  String name
  String value
  Object payload
}
