package io.muoncore.codec.json
import io.muoncore.codec.MyTestClass
import spock.lang.Specification

import static io.muoncore.codec.types.MuonCodecTypes.listOf
import static io.muoncore.codec.types.MuonCodecTypes.mapOf

class GsonCodecSpec extends Specification {

  def "gson codec converts object to byte array"() {
    def codec = new GsonCodec()

    when:
    byte[] val = codec.encode(new MyTestClass(someValue: "hello", someOtherValue: 43))

    then:
    new String(val) == """{"someValue":"hello","someOtherValue":43}"""
  }

  def "when passed incompatible type, throws DecodingFailureException"() {
    def codec = new GsonCodec()

    when:
    def value = codec.decode("""{"someValue":"hello","someOtherValue":43}""".bytes
            , MyTestClass)

    then:
    assertMyTestClass(value, "hello", 43)
  }


  def "gson codec converts json string to object"() {
    def codec = new GsonCodec()

    when:
    def value = codec.decode("""{"someValue":"hello","someOtherValue":43}""".bytes
            , MyTestClass)

    then:
    assertMyTestClass(value, "hello", 43)
  }

  def "gson codec decodes Lists of Objects"() {
    def codec = new GsonCodec()

    when:
    def value = codec.decode("""[{"someValue":"hello","someOtherValue":43},{"someValue":"hello2","someOtherValue":42}]""".bytes
            , listOf(MyTestClass))

    then:
    value instanceof List
    assertMyTestClass(value[0], "hello", 43)
    assertMyTestClass(value[1], "hello2", 42)
  }

  def "gson codec decodes Map of Lists of Objects"() {
    def codec = new GsonCodec()


    def list1 = """[{"someValue":"hello1","someOtherValue":41},{"someValue":"hello2","someOtherValue":42}]"""
    def list2 = """[{"someValue":"hello3","someOtherValue":43},{"someValue":"hello4","someOtherValue":44}]"""
    when:
    def value = codec.decode("{\"list1\":${list1}, \"list2\":${list2}}" as byte[],
            mapOf(String, listOf(MyTestClass)))

    then:
    value instanceof Map
    value['list1'] instanceof List

    assertMyTestClass(value['list1'][0], "hello1", 41)
    assertMyTestClass(value['list1'][1], "hello2", 42)

    value['list2'] instanceof List
    assertMyTestClass(value['list2'][0], "hello3", 43)
    assertMyTestClass(value['list2'][1], "hello4", 44)

  }

  private static void assertMyTestClass(def value, def stringValue, def intValue) {
    assert value instanceof MyTestClass
    assert value.someValue == stringValue
    assert value.someOtherValue == intValue
  }

}

