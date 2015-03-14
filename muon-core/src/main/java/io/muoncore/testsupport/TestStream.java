package io.muoncore.testsupport;

import io.muoncore.MuonStreamGenerator;
import org.reactivestreams.Publisher;

import java.util.Map;

public class TestStream implements MuonStreamGenerator {

    @Override
    public Publisher generatePublisher(Map<String, String> parameters) {
        return null;
    }
}
