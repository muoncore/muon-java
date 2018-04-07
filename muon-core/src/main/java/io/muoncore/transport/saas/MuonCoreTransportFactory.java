package io.muoncore.transport.saas;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.MuonTransportFactory;

import java.util.Properties;

public class MuonCoreTransportFactory implements MuonTransportFactory {
  @Override
  public MuonTransport build(Properties properties) {
    return new MuonCoreTransport();
  }

  @Override
  public void setAutoConfiguration(AutoConfiguration autoConfiguration) {

  }
}
