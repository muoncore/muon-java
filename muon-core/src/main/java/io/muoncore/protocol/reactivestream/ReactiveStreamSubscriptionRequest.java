package io.muoncore.protocol.reactivestream;

import java.util.HashMap;
import java.util.Map;

public class ReactiveStreamSubscriptionRequest {

    private Map<String, Object> args = new HashMap<>();

    public void arg(String name, String value) {
        args.put(name, value);
    }

    public Map<String, Object> getArgs() {
        return args;
    }


}
