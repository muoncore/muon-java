package io.muoncore.protocol.introspection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemaIntrospectionRequest {
  private String protocol;
  private String endpoint;
}
