package io.muoncore.codec

import spock.lang.Specification

class GsonTextCodecSpec extends Specification {

  def "gson codec converts object to json string"() {
    def codec = new GsonTextCodec()

    expect:
    codec.encode(new MyTestClass(someValue: "hello", someOtherValue: 43)) ==
        """{"someValue":"hello","someOtherValue":43}"""
  }
  def "gson codec converts json string to object"() {
    def codec = new GsonTextCodec()

    when:
    def value = codec.decode("""{"someValue":"hello","someOtherValue":43}""", MyTestClass)

    then:
    value instanceof MyTestClass
    value.someValue == "hello"
    value.someOtherValue == 43
  }

  def "gson codec converts json string to a map"() {
    def codec = new GsonTextCodec()

    when:
    def value = codec.decode("""{"someValue":"hello","someOtherValue":43}""")

    then:
    value instanceof Map
    value.someValue == "hello"
    value.someOtherValue == 43
  }
}

