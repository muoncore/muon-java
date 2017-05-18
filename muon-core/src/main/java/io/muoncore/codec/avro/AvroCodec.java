package io.muoncore.codec.avro;

import io.muoncore.codec.Codecs;
import io.muoncore.codec.MuonCodec;
import io.muoncore.exception.MuonEncodingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.FileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SuppressWarnings("all")
public class AvroCodec implements MuonCodec {

  private Map<Class, Schema> schemas = new HashMap<>();
  private ReflectData RD = ReflectData.AllowNull.get();

  private final static Map<Class, AvroConverter> converterMapping = new HashMap<>();

  public static void registerConverter(Class clType, AvroConverter converter) {
    converterMapping.put(clType, converter);
  }

  @Override
  public <T> T decode(byte[] encodedData, Type type) {
    final Class clType = (Class) type;
    final AvroConverter converter = converterMapping.get(clType);

    if (converter == null) {
      if (!SpecificRecord.class.isAssignableFrom(clType)) {
        return decodePojo(encodedData, clType);
      }

      return decodeSpecificType(encodedData, clType);
    } else {
      return converter.decode(encodedData);
    }
  }

  private <T> T decodePojo(byte[] encodedData, Class type) {

    log.debug("Reflecting decode of {}, consider registering a converter", type);

    DatumReader<T> userDatumReader = RD.createDatumReader(RD.getSchema(type));

    try {
      FileReader<T> ts = DataFileReader.openReader(new SeekableByteArrayInput(encodedData), userDatumReader);
      T mine = (T) type.newInstance();

      return (T) ts.next(mine);
    } catch (IOException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
    }

    return null;
  }

  private <T> T decodeSpecificType(byte[] encodedData, Class type) {
    DatumReader<SpecificRecord> userDatumReader = new SpecificDatumReader<>((Class<SpecificRecord>) type);

    try {
      FileReader<SpecificRecord> ts = DataFileReader.openReader(new SeekableByteArrayInput(encodedData), userDatumReader);
      SpecificRecord mine = (SpecificRecord) type.newInstance();

      return (T) ts.next(mine);
    } catch (IOException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public byte[] encode(Object data) throws UnsupportedEncodingException {
    final Class clType = data.getClass();
    final AvroConverter converter = converterMapping.get(clType);

    try {
      if (converter != null) {
        log.info("Got converter of type {} for {}", converter.getClass(), clType);
        return converter.encode(data);
      } else if (!(data instanceof SpecificRecord)) {
        return encodePojo(data);
      }

      return encodeSpecificRecord(data);
    } catch (AvroRuntimeException e) {
      throw new MuonEncodingException("AvroCode is Unable to encode " + data, e);
    }
  }

  private byte[] encodePojo(Object data) {

    log.debug("Reflecting encode of {}, {}, consider registering a converter", data.getClass(), data);

    Schema schema = RD.getSchema(data.getClass());

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    DatumWriter userDatumWriter = RD.createDatumWriter(schema);
    DataFileWriter dataFileWriter = new DataFileWriter(userDatumWriter);
    try {
      dataFileWriter.create(schema, byteArrayOutputStream);
      dataFileWriter.append(data);
      dataFileWriter.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
    return byteArrayOutputStream.toByteArray();
  }

  private byte[] encodeSpecificRecord(Object data) {
    SpecificRecord record = (SpecificRecord) data;

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    DatumWriter userDatumWriter = new SpecificDatumWriter(data.getClass());
    DataFileWriter dataFileWriter = new DataFileWriter(userDatumWriter);
    try {
      dataFileWriter.create(record.getSchema(), byteArrayOutputStream);
      dataFileWriter.append(data);
      dataFileWriter.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
    return byteArrayOutputStream.toByteArray();
  }

  @Override
  public String getContentType() {
    return "avro";
  }

  @Override
  public boolean hasSchemasFor(Class type) {
    if (type.getCanonicalName().startsWith("java.util") && converterMapping.get(type) == null) {
      log.debug("Avro cannot provide schemas for a java.util type {}", type);
      return false;
    }
    return true;
  }

  @Override
  public Codecs.SchemaInfo getSchemaInfoFor(Class type) {
    try {
      //TODO, allow loading in existing schemas.
      return new Codecs.SchemaInfo(RD.getSchema(type).toString(), "avro");
    } catch (AvroRuntimeException e) {
      throw new MuonEncodingException("Unable to generate an AvroSchema for type " + type + " this is most like due to missing generic types on a POJO. Considering generating a SpecificRecord or registering a Converter", e);
    }
  }

  @Override
  public boolean canEncode(Class type) {
    return hasSchemasFor(type);
  }
}
