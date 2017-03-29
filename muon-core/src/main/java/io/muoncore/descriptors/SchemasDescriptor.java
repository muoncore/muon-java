package io.muoncore.descriptors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SchemasDescriptor {
  private String protocol;
  private String resource;
  private Map<String, SchemaDescriptor> schemas;
}
