package io.muoncore.internal;

import io.muoncore.MuonStreamGenerator;
import org.reactivestreams.Publisher;

import java.util.Map;

public class MuonStreamExistingGenerator implements MuonStreamGenerator {

    private Publisher publisher;

    public MuonStreamExistingGenerator(Publisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public Publisher generatePublisher(Map<String, String> parameters) {
        return publisher;
    }
}
