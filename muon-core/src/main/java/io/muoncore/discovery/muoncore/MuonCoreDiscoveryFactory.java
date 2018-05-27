package io.muoncore.discovery.muoncore;

import io.muoncore.Discovery;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.discovery.DiscoveryFactory;

import java.util.Properties;

public class MuonCoreDiscoveryFactory implements DiscoveryFactory {

  private MuonCoreConnection connection;

  @Override
  public Discovery build(Properties properties) {
    return new MuonCoreDiscovery(connection);
  }

  @Override
  public void setAutoConfiguration(AutoConfiguration autoConfiguration) {
    ///TODO... extract the app key to use!
    this.connection = MuonCoreConnection.extractFromAutoConfig(autoConfiguration);
  }
}
