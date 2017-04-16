package io.muoncore.codec.avro

import io.muoncore.codec.Codecs
import io.muoncore.messages.AnotherMuonMessage
import spock.lang.Specification

import static io.muoncore.codec.avro.AvroCodec.registerConverter

class AvroCodecRegister extends Specification {
  def "can do encoding roundtrip with a registered converter"() {
    def converter = new TestConverter()
    registerConverter(YetAnotherMuonMessage, converter)
    registerConverter(HashMap, converter)
    def codec = new AvroCodec()

    when:

    def ret = codec.encode(new YetAnotherMuonMessage("1", "2", 3))
    def decode = codec.decode(ret, YetAnotherMuonMessage)
    def schemaInfo = converter.getSchemaInfoFor(YetAnotherMuonMessage)

    then:
    converter.hasSchemasFor(YetAnotherMuonMessage)
    !converter.hasSchemasFor(HashMap)
    codec.hasSchemasFor(YetAnotherMuonMessage)
    codec.hasSchemasFor(HashMap)
    !codec.hasSchemasFor(TreeMap)

    decode instanceof AnotherMuonMessage
    decode.username as String == "1"
    decode.tweet as String == "2"
    decode.timestamp as long == 3

    schemaInfo.schemaText == codec.getSchemaInfoFor(AnotherMuonMessage).schemaText
    schemaInfo.schemaType == codec.getSchemaInfoFor(AnotherMuonMessage).schemaType
  }

  class YetAnotherMuonMessage extends AnotherMuonMessage {
    YetAnotherMuonMessage(username, tweet, timestamp) {
      super(username, tweet, timestamp)
    }
  }

  class TestConverter implements AvroConverter {
    def codec = new AvroCodec()

    @Override
    <T> T decode(byte[] encodedData) {
      codec.decode(encodedData, AnotherMuonMessage)
    }

    @Override
    byte[] encode(Object data) {
      def dataPOJOI = (YetAnotherMuonMessage) data
      def dataPOJO = new AnotherMuonMessage(dataPOJOI.username, dataPOJOI.tweet, dataPOJOI.timestamp)
      codec.encode(dataPOJO)
    }

    @Override
    Codecs.SchemaInfo getSchemaInfoFor(Class type) {
      if (type == YetAnotherMuonMessage) {
        codec.getSchemaInfoFor(AnotherMuonMessage)
      } else {
        codec.getSchemaInfoFor(type)
      }
    }

    @Override
    boolean hasSchemasFor(Class type) {
      type == YetAnotherMuonMessage
    }
  }
}
