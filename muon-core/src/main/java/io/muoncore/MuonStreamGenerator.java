package io.muoncore;

import org.reactivestreams.Publisher;

import java.util.Map;

public interface MuonStreamGenerator<T> {

    Publisher<T> generatePublisher(Map<String, String> parameters);

}
