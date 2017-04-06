package io.muoncore.codec.json;

import com.google.gson.Gson;
import io.muoncore.codec.Codecs;
import io.muoncore.codec.MuonCodec;
import io.muoncore.exception.MuonException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

public class GsonCodec implements MuonCodec {

  private Gson gson = new Gson();

  @Override
  public <T> T decode(byte[] encodedData, Type type) {
    try {
      return gson.fromJson(new String(encodedData, "UTF8"), type);
    } catch (UnsupportedEncodingException e) {
      throw new MuonException("Unable to read byte array", e);
    }
  }

  @Override
  public byte[] encode(Object data) throws UnsupportedEncodingException {
    return gson.toJson(data).getBytes("UTF8");
  }

  @Override
  public String getContentType() {
    return "application/json";
  }

  @Override
  public boolean hasSchemasFor(Class type) {
    return false;
  }

  @Override
  public Codecs.SchemaInfo getSchemaInfoFor(Class type) {
    return null;
  }

  @Override
  public boolean canEncode(Class type) {
    return true;
  }
}
