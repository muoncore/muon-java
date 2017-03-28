package io.muoncore.avro.codec;

import io.muoncore.codec.MuonCodec;
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
import sun.reflect.misc.ReflectUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class AvroCodec implements MuonCodec {

  private Map<Class, Schema> schemas = new HashMap<>();

  @Override
  public <T> T decode(byte[] encodedData, Type type) {
    System.out.println(new String(encodedData));

    if (!SpecificRecord.class.isAssignableFrom((Class) type)) {
      return decodePojo(encodedData, (Class) type);
    }

    return decodeSpecificType(encodedData, (Class) type);
  }

  private <T> T decodePojo(byte[] encodedData, Class type) {

    DatumReader<T> userDatumReader = ReflectData.get().createDatumReader(ReflectData.get().getSchema(type));

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
    Schema schema = ReflectData.get().getSchema(data.getClass());

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    DatumWriter userDatumWriter = ReflectData.get().createDatumWriter(schema);
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
}
