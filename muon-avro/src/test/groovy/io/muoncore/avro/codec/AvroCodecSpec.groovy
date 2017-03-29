package io.muoncore.avro.codec

import io.muoncore.messages.AnotherMuonMessage
import io.muoncore.messages.MuonMessage
import org.apache.avro.Schema
import org.apache.avro.reflect.ReflectData
import spock.lang.Specification

class AvroCodecSpec extends Specification {

  def "can do encoding roundtrip for a specific record"() {
    def codec = new AvroCodec()

    when:
    def ret = codec.encode(new MuonMessage("YO DUDE!", "the master tweet", 12))

    def decode = codec.decode(ret,  MuonMessage)

    then:
    decode instanceof MuonMessage
    decode.username as String == "YO DUDE!"
    decode.tweet as String == "the master tweet"
    decode.timestamp == 12

  }

  def "can do encoding roundtrip for a generic pojo"() {
    def codec = new AvroCodec()

    when:
    def ret = codec.encode(new AnotherMuonMessage("YO DUDE!", "the master tweet", 12))

    def decode = codec.decode(ret,  AnotherMuonMessage)

    then:
    decode instanceof AnotherMuonMessage
    decode.username as String == "YO DUDE!"
    decode.tweet as String == "the master tweet"
    decode.timestamp == 12

  }

  def "geerate schema"() {
    def r = ReflectData.newInstance()
    Schema schema = r.getSchema(AwesomeClass)

    println schema

    expect:
    1==2
  }
}


//BREABREABREAK

/**
 * TODO
 * check the schema type?
 *
 * be able to download the schemas in a generialised form. maybe another interface?
 *
 * ponder the introspection. how should schemas be requested and transported around.  How can we initialise them?
 * Consider that a particular Avro schema could be loaded into the codec for matching against classes.
 *
 * Consider versioning of messages.
 *
 * How can avro support be advertised in the disco?
 */






class AwesomeClass {

  String name
  String other
  int nyMun
  double value

}
