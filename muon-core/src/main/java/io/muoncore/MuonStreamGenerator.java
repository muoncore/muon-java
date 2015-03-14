package io.muoncore;

import org.reactivestreams.Publisher;

import java.util.Map;

public interface MuonStreamGenerator {

    Publisher generatePublisher(Map<String, String> parameters);

}
