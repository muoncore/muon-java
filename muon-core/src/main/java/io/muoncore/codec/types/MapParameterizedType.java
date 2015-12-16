package io.muoncore.codec.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

public class MapParameterizedType implements ParameterizedType {

    private final Type valueType;
    private final Type keyType;

    public MapParameterizedType(Type keyType, Type valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{keyType, valueType};
    }

    @Override
    public Type getRawType() {
        return HashMap.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapParameterizedType that = (MapParameterizedType) o;

        if (valueType != null ? !valueType.equals(that.valueType) : that.valueType != null) return false;
        return !(keyType != null ? !keyType.equals(that.keyType) : that.keyType != null);

    }

    @Override
    public int hashCode() {
        int result = valueType != null ? valueType.hashCode() : 0;
        result = 31 * result + (keyType != null ? keyType.hashCode() : 0);
        return result;
    }
}
