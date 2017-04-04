package io.muoncore.codec.avro;

import io.muoncore.codec.Codecs;
import io.muoncore.codec.MuonCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.FileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.io.*;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SuppressWarnings("all")
public class AvroCodec implements MuonCodec {

  private Map<Class, Schema> schemas = new HashMap<>();
  private ReflectData RD = ReflectData.AllowNull.get();

  @Override
  public <T> T decode(byte[] encodedData, Type type) {
    if (!SpecificRecord.class.isAssignableFrom((Class) type)) {
      return decodePojo(encodedData, (Class) type);
    }

    return decodeSpecificType(encodedData, (Class) type);
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

    if (!(data instanceof SpecificRecord)) {
      return encodePojo(data);
    }

    return encodeSpecificRecord(data);
  }

  private byte[] encodePojo(Object data) {

    log.info("Reflecting encode of {}, {}, consider registering a converter", data.getClass(), data);

    Type type = data.getClass();

    if (data.getClass().getGenericSuperclass() instanceof ParameterizedType) {
      type = ((ParameterizedType) data.getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];

      log.info("Implements {}", type.getTypeName());
    }

    Schema schema = RD.getSchema(type);

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
    if (type.getCanonicalName().startsWith("java.util")) {
      log.debug("Avro cannot provide schemas for a java.util type {}", type);
      return false;
    }
    return true;
  }

  @Override
  public Codecs.SchemaInfo getSchemaInfoFor(Class type) {
    //TODO, allow loading in existing schemas.
    return new Codecs.SchemaInfo(RD.getSchema(type).toString(), "avro");
  }
}
