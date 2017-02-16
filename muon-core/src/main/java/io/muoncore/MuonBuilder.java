package io.muoncore;

import io.muoncore.codec.Codecs;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.discovery.DiscoveryFactory;
import io.muoncore.discovery.MultiDiscovery;
import io.muoncore.exception.MuonException;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.MuonTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MuonBuilder {

    private Logger LOG = LoggerFactory.getLogger(MuonBuilder.class.getSimpleName());

    private AutoConfiguration config;

    private Codecs codecs = new JsonOnlyCodecs();

    private MuonBuilder(AutoConfiguration config) {
        this.config = config;
    }

    public static MuonBuilder withConfig(AutoConfiguration config) {
        return new MuonBuilder(config);
    }

    public MuonBuilder withCodecs(Codecs codecs) {
      this.codecs = codecs;
      return this;
    }

    public Muon build() {
        try {
            return new MultiTransportMuon(config, generateDiscovery(), generateTransport(), codecs);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new MuonException("Unable to create Muon instance, error during construction", e);
        }
    }

    private List<MuonTransport> generateTransport() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String[] factoryImpl = config.getStringConfig("muon.transport.factories").split(",");

        List<MuonTransport> transports = new ArrayList<>();

        for(String factory: factoryImpl) {
            try {
                MuonTransportFactory factoryInstance = (MuonTransportFactory) Class.forName(factory).newInstance();
                factoryInstance.setAutoConfiguration(config);
                transports.add(factoryInstance.build(config.getProperties()));
            } catch (ClassNotFoundException ex) {
                LOG.info("Configured transport " + factory + " not present in the classpath, ignoring");
            }
        }

        return transports;
    }

    private Discovery generateDiscovery() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String[] factoryImpl = config.getStringConfig("muon.discovery.factories").split(",");

        List<Discovery> discoveries = new ArrayList<>();

        for(String factory: factoryImpl) {
            try {
                DiscoveryFactory factoryInstance = (DiscoveryFactory) Class.forName(factory).newInstance();
                factoryInstance.setAutoConfiguration(config);
                discoveries.add(factoryInstance.build(config.getProperties()));
            } catch (ClassNotFoundException ex) {
                LOG.info("Configured discovery " + factory + " not present in the classpath, ignoring");
            }
        }

        return new MultiDiscovery(discoveries);
    }
}
