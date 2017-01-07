package io.muoncore.discovery.multicast;

import io.muoncore.Discovery;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.discovery.DiscoveryFactory;
import io.muoncore.transport.ServiceCache;

import java.util.Properties;


public class MulticastDiscoveryFactory implements DiscoveryFactory {

  @Override
  public Discovery build(Properties properties) {
    return new MulticastDiscovery(new ServiceCache());
  }

  @Override
  public void setAutoConfiguration(AutoConfiguration autoConfiguration) {}
}
