package org.muoncore.codec

import spock.lang.Specification

class Base64TextCodecSpec extends Specification {

  def "base64 text codec converts object to json string"() {
    def mockCodec = Mock(BinaryCodec) {
      encode(_) >> "HH".getBytes("UTF8") // SEg= in base 64
    }

    def codec = new Base64TextCodec(mockCodec)

    expect:
    codec.encode(new Object()) == "SEg="
  }


  def "base64 codec converts to object"() {
    def mockCodec = Mock(BinaryCodec) {
      decode("HH".getBytes("UTF8"), _) >> new MyTestClass()
    }

    def codec = new Base64TextCodec(mockCodec)

    when:
    def value = codec.decode("SEg=", MyTestClass)

    then:
    value instanceof MyTestClass

  }

  def "gson codec converts json string to a map"() {
    def mockCodec = Mock(BinaryCodec) {
      decode("HH".getBytes("UTF8")) >> new HashMap()
    }

    def codec = new Base64TextCodec(mockCodec)

    when:
    def value = codec.decode("SEg=")

    then:
    value instanceof Map
  }
}

