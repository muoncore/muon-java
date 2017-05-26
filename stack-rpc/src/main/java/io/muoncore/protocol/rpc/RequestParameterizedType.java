package io.muoncore.protocol.rpc;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class RequestParameterizedType implements ParameterizedType {

    private Type type;

    public RequestParameterizedType(Type type) {
        this.type = type;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{type};
    }

    @Override
    public Type getRawType() {
        return Request.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestParameterizedType that = (RequestParameterizedType) o;

        return !(type != null ? !type.equals(that.type) : that.type != null);
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
