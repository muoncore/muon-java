package io.muoncore.crud.internal;

import io.muoncore.MuonStreamGenerator;
import org.reactivestreams.Publisher;

import java.util.Map;

public class MuonStreamExistingGenerator<T> implements MuonStreamGenerator<T> {

    private Publisher<T> publisher;

    public MuonStreamExistingGenerator(Publisher<T> publisher) {
        this.publisher = publisher;
    }

    @Override
    public Publisher<T> generatePublisher(Map<String, String> parameters) {
        return publisher;
    }
}
