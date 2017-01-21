package io.muoncore.protocol.reactivestream.messages;

import io.muoncore.protocol.Auth;

import java.util.HashMap;
import java.util.Map;

public class ReactiveStreamSubscriptionRequest {

  private Auth auth;
  private String streamName;
  private Map<String, Object> args = new HashMap<>();

  public ReactiveStreamSubscriptionRequest(String streamName, Auth auth) {
    this.auth = auth;
    this.streamName = streamName;
  }

  public ReactiveStreamSubscriptionRequest(String streamName) {
    this.streamName = streamName;
  }

  public Auth getAuth() {
    return auth;
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
