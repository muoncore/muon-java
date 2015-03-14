package org.muoncore.codec;

import java.util.*;

public class Codecs {

    private TextCodec defaultTextCodec;
    private BinaryCodec defaultBinaryCodec;

    private Map<Class, TextCodec> textCodecLookup = new HashMap<Class, TextCodec>();
    private Map<Class, BinaryCodec> binaryCodecLookup = new HashMap<Class, BinaryCodec>();

    public static Codecs defaults() {
        Codecs codecs = new Codecs();

        codecs.defaultBinaryCodec = new TextBinaryCodec(new GsonTextCodec());
        codecs.defaultTextCodec = new GsonTextCodec();

        return codecs;
    }

    public Codecs withClassCodec(Class type, BinaryCodec binaryCodec) {
        binaryCodecLookup.put(type, binaryCodec);
        return this;
    }
    public Codecs withClassCodec(Class type, TextCodec textCodec) {
        textCodecLookup.put(type, textCodec);
        return this;
    }

    //TODO, pass in the Accepts header, use that to choose a codec and then
    //somehow inform the caller that this has been chosen. Or something...
    public String encodeToString(Object object) {
        TextCodec codec = textCodecLookup.get(object.getClass());
        if (codec == null) { codec = defaultTextCodec; }
        return codec.encode(object);
    }

    //TODO, pass in the Accepts header, use that to choose a codec and then
    //somehow inform the caller that this has been chosen. Or something...
    public byte[] encodeToByte(Object object) {
        BinaryCodec codec = binaryCodecLookup.get(object.getClass());
        if (codec == null) { codec = defaultBinaryCodec; }
        return codec.encode(object);
    }

    public <T> T decodeObject(String source, String contentType, Class<T> type) {
        TextCodec codec = textCodecLookup.get(type);
        if (codec == null) { codec = defaultTextCodec; }
        return codec.decode(source, type);
    }

    public <T> T decodeObject(byte[] source, String contentType, Class<T> type) {
        BinaryCodec codec = binaryCodecLookup.get(type);
        if (codec == null) { codec = defaultBinaryCodec; }
        return codec.decode(source, type);
    }

    public Set<String> getTextContentTypesAvailable(Class type) {
        Set<String> ret = new HashSet<String>();
        TextCodec codec = textCodecLookup.get(type);
        if (codec != null) { ret.add(codec.getContentType()); }
        ret.add(defaultTextCodec.getContentType());
        return ret;
    }

    public Set<String> getBinaryContentTypesAvailable(Class type) {
        Set<String> ret = new HashSet<String>();
        BinaryCodec codec = binaryCodecLookup.get(type);
        if (codec != null) { ret.add(codec.getContentType()); }
        ret.add(defaultBinaryCodec.getContentType());
        return ret;
    }

    public String getBinaryContentType(Class type) {
        BinaryCodec codec = binaryCodecLookup.get(type);
        if (codec != null) {
            return codec.getContentType();
        }
        return defaultBinaryCodec.getContentType();
    }

    public String getStringContentType(Class type) {
        TextCodec codec = textCodecLookup.get(type);
        if (codec != null) {
            return codec.getContentType();
        }
        return defaultTextCodec.getContentType();
    }
}
