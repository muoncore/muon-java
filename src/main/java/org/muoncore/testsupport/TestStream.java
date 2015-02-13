package org.muoncore.testsupport;

import org.muoncore.MuonStreamGenerator;
import org.reactivestreams.Publisher;

import java.util.Map;

public class TestStream implements MuonStreamGenerator {

    @Override
    public Publisher generatePublisher(Map<String, String> parameters) {
        return null;
    }
}
