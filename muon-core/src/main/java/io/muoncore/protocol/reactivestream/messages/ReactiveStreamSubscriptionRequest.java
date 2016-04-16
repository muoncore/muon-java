package io.muoncore.protocol.reactivestream.messages;

import java.util.HashMap;
import java.util.Map;

public class ReactiveStreamSubscriptionRequest {

    private String streamName;
    private Map<String, Object> args = new HashMap<>();

    public ReactiveStreamSubscriptionRequest(String streamName) {
        this.streamName = streamName;
    }

    public String getStreamName() {
        return streamName;
    }

    public void arg(String name, String value) {
        args.put(name, value);
    }

    public Map<String, Object> getArgs() {
        return args;
    }


}
