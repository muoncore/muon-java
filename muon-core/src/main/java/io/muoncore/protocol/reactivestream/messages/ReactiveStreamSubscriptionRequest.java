package io.muoncore.protocol.reactivestream.messages;

import io.muoncore.protocol.Auth;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Getter
public class ReactiveStreamSubscriptionRequest {

  private Auth auth;
  private String streamName;
  private Map<String, String> args = new HashMap<>();

  public ReactiveStreamSubscriptionRequest(String streamName, Auth auth) {
    this.auth = auth;
    this.streamName = streamName;
  }

  public ReactiveStreamSubscriptionRequest(String streamName) {
    this.streamName = streamName;
  }

  public void arg(String name, String value) {
    args.put(name, value);
  }
}
