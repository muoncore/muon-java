package io.muoncore;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.net.URI;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
@ToString(of = {"instanceId", "identifier", "tags"})
public class InstanceDescriptor {

  private String instanceId;
  private String identifier;
  private List<String> tags;
  private List<String> codecs;
  private List<URI> connectionUrls;
  private Collection<String> capabilities;

  public void validate() {
    if (instanceId == null) {
      instanceId = identifier;
    }
  }
}
