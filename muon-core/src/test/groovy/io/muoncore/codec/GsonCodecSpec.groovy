package io.muoncore.codec

import spock.lang.Specification

class GsonCodecSpec extends Specification {

  def "gson codec converts object to byte array"() {
    def codec = new GsonCodec()

    when:
    byte[] val = codec.encode(new MyTestClass(someValue: "hello", someOtherValue: 43))

    then:
    new String(val) == """{"someValue":"hello","someOtherValue":43}"""
  }

  def "gson codec converts json string to object"() {
    def codec = new GsonCodec()

    when:
    def value = codec.decode("""{"someValue":"hello","someOtherValue":43}""".bytes
        , MyTestClass)

    then:
    value instanceof MyTestClass
    value.someValue == "hello"
    value.someOtherValue == 43
  }

  def "gson codec converts json string to a map"() {
    def codec = new GsonCodec()

    when:
    def value = codec.decode("""{"someValue":"hello","someOtherValue":43}""".bytes)

    then:
    value instanceof Map
    value.someValue == "hello"
    value.someOtherValue == 43
  }
}
