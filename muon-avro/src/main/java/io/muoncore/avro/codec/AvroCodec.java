package io.muoncore.avro.codec;

import io.muoncore.codec.MuonCodec;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.FileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

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
    DatumReader<SpecificRecord> userDatumReader = new SpecificDatumReader<>((Class<SpecificRecord>) type);

    try {
      FileReader<SpecificRecord> ts = DataFileReader.openReader(new SeekableByteArrayInput(encodedData), userDatumReader);
      SpecificRecord mine = (SpecificRecord) ((Class) type).newInstance();

      return (T) ts.next(mine);
    } catch (IOException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public byte[] encode(Object data) throws UnsupportedEncodingException {
    if (!(data instanceof SpecificRecord)) {
      throw new IllegalArgumentException("Unable to encode object of type " + data.getClass() + ". Avro Codec can only handle types of SpecificRecord");
    }

    SpecificRecord record = (SpecificRecord) data;
    Schema schema = record.getSchema();

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
