package io.muoncore.codec.kryo

import spock.lang.Specification

class KryoBinaryCodecSpec extends Specification {

  def "codec can encode and decode an object"() {

    def codec = new KryoBinaryCodec()

    when:
    byte[] encoded = codec.encode(new MyObj(value: "Something Cool!", otherValue: 1234))
    def decoded = codec.decode(encoded, MyObj)

    then:
    decoded.otherValue == 1234
    decoded.value == "Something Cool!"
  }
}

class MyObj {
  String value
  int otherValue
}