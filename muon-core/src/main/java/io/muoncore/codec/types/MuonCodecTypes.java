package io.muoncore.codec.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class MuonCodecTypes {

    public static ParameterizedType listOf(Type type) {
        return new ListParameterizedType(type);
    }

    public static ParameterizedType mapOf(Type keyType, Type valueType) {
        return new MapParameterizedType(keyType, valueType);
    }
}
