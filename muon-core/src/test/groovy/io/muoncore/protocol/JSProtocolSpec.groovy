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

class JSProtocolSpec extends Specification {

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

    def proto = new JSProtocol(
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

    def proto = new JSProtocol(
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
    def proto = new JSProtocol(
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
    def proto = new JSProtocol(
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
    def proto = new JSProtocol(
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
    def proto = new JSProtocol(
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
      ret << it
    }
    transportchannel.right().receive {
      ret << it
    }

    when:
    def proto = new JSProtocol(
      muon, "faked", channel.right()
    )
    proto.start(JS_SHUTDOWN)

    channel.left().send("Hello")

    then:
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
    def proto = new JSProtocol(
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
    def proto = new JSProtocol(
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
    def proto = new JSProtocol(
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
    def proto = new JSProtocol(
      muon, "faked", channel.right()
    )
    proto.start(JS_API_ENCODE)

    channel.left().send(new MyResponseType(name: "HELLO!", payload: ["hello":"AWESOME"]))

    then:
    new PollingConditions().eventually {
      ret instanceof MuonOutboundMessage
      println new String(ret.payload)
      def decoded = muon.getCodecs().decode(ret.payload, ret.contentType, MyResponseType)
      decoded.name == "HELLO!"
      decoded.payload.hello == "AWESOME"
    }
  }

  static JS_RECIEVE = """
function fromApi(msg) {
  sendApi("YO, DUDE")
}"""

  static JS_CONFIG = """
function fromApi(msg) {
  print("CALLING SERVICE " + serviceName())
  sendApi(serviceName())
}"""

  static JS_SENDTRANSPORT = """
function fromApi(msg) {
  print("CALLING SERVICE " + serviceName())
  sendTransport({"hello":"SIMPLES"})
  sendTransport({"hello":"SIMPLES"})
}"""
  static JS_RECTRANSPORT = """
var transportmessages = []

function fromApi(msg) {
  
}
function fromTransport(msg) {
  transportmessages.push(msg)
  if (transportmessages.length == 3) {
   sendApi(3)
  }
}"""

  static JS_TIMED_MESSAGE_API = """
function fromApi(msg) {
  setTimeout(function() { 
     sendApi("YO")
  }, 100);
}
function fromTransport(msg) {

}"""

  static JS_TIMED_MESSAGE_CANCEL = """
function fromApi(msg) {
  var handle = setTimeout(function() { 
     sendApi("YO")
  }, 100);
  
  clearTimeout(handle)
}
function fromTransport(msg) {

}"""

  static JS_SHUTDOWN = """
function fromApi(msg) {
  shutdown()
}
function fromTransport(msg) {

}"""
  static JS_TRANSPORT_MUON_MESSAGE = """
function fromApi(msg) {
  sendTransport({
    payload: {
    },
    target:"awesome",
    step: "cool"
  })
}
function fromTransport(msg) {

}"""
  static JS_API_MUON_MESSAGE = """
function fromApi(msg) {
  
}
function fromTransport(msg) {
sendApi(type("MyResponseType", {
    name: "cool",
    value: "simple"
  }))
}"""

  static JS_API_DECODE = """
function fromApi(msg) {
}
function fromTransport(msg) {
  var resp = decode("MyResponseTypeWrapped", msg)
  sendApi(type("MyResponseType", {
    name: "Hello",
    payload: resp
  }))
}"""

  static JS_API_ENCODE = """
function fromApi(msg) {
  var resp = encodeFor(msg, "myservice")
  sendTransport({
    payload: resp.payload,
    contentType: resp.contentType
  })
}
function fromTransport(msg) {
  
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
