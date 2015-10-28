package io.muoncore.codec.json;

import com.google.gson.Gson;
import io.muoncore.codec.MuonCodec;
import io.muoncore.exception.MuonException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class GsonCodec implements MuonCodec {

    private Gson gson = new Gson();

    @Override
    public <T> T decode(byte[] encodedData, Class<T> type) {
        try {
            return gson.fromJson(new String(encodedData, "UTF8"), type);
        } catch (UnsupportedEncodingException e) {
            throw new MuonException("Unable to read byte array", e);
        }
    }

    @Override
    public Map decode(byte[] encodedData) {
        try {
            return gson.fromJson(new String(encodedData, "UTF8"), Map.class);
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
}
