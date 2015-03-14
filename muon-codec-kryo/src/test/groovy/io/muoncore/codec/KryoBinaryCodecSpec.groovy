package io.muoncore.codec

import spock.lang.Specification

class KryoBinaryCodecSpec extends Specification {

  def "codec cannot decode to map"() {
    when:
    new KryoBinaryCodec().decode(new byte[0])

    then:
    thrown(IllegalArgumentException)
  }

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