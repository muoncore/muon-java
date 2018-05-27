package io.muoncore.transport.saas;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.discovery.muoncore.MuonCoreConnection;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.MuonTransportFactory;

import java.util.Properties;

public class MuonCoreTransportFactory implements MuonTransportFactory {

  private MuonCoreConnection connection;

  @Override
  public MuonTransport build(Properties properties) {
    return new MuonCoreTransport(connection);
  }

  @Override
  public void setAutoConfiguration(AutoConfiguration autoConfiguration) {
    this.connection = MuonCoreConnection.extractFromAutoConfig(autoConfiguration);
  }
}
