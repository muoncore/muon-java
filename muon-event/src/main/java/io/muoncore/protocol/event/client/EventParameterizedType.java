package io.muoncore.protocol.event.client;

import io.muoncore.protocol.event.Event;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class EventParameterizedType implements ParameterizedType {

    private Type type;

    public EventParameterizedType(Type type) {
        this.type = type;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{type};
    }

    @Override
    public Type getRawType() {
        return Event.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventParameterizedType that = (EventParameterizedType) o;

        return !(type != null ? !type.equals(that.type) : that.type != null);

    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
