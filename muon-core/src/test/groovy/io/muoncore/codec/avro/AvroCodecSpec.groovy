package io.muoncore.codec.avro

import io.muoncore.messages.AnotherMuonMessage
import io.muoncore.messages.MuonMessage
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
}

