package org.muoncore.codec;

import com.google.gson.Gson;

import java.util.Map;

public class GsonTextCodec implements TextCodec {

    private Gson gson = new Gson();

    @Override
    public Map decode(String encodedData) {
        return gson.fromJson(encodedData, Map.class);
    }

    @Override
    public String encode(Object data) {
        return gson.toJson(data);
    }

    @Override
    public <T> T decode(String encodedData, Class<T> type) {
        return gson.fromJson(encodedData, type);
    }
}
