package io.muoncore.descriptors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaDescriptor {
  private String name;
  private String schema;
  private String type;
}
