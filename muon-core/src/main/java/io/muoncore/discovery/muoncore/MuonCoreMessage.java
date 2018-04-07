package io.muoncore.discovery.muoncore;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MuonCoreMessage {
  private String type;
  private String step;
  private String correlationId;
  private byte[] data;
}
