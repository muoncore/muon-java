package io.muoncore.api;

@FunctionalInterface
public interface PromiseFunction<T> {
    void call(T arg);
}
