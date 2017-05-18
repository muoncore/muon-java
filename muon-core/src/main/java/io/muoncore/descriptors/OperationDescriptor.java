package io.muoncore.descriptors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OperationDescriptor {

  private String resource;
  private String doc;

  public OperationDescriptor(String resource) {
    this.resource = resource;
  }
}
