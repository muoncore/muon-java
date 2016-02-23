package io.muoncore;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.discovery.DiscoveryFactory;
import io.muoncore.discovery.MultiDiscovery;
import io.muoncore.exception.MuonException;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.MuonTransportFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MuonBuilder {

    private Logger LOG = Logger.getLogger(MuonBuilder.class.getSimpleName());

    private AutoConfiguration config;

    private MuonBuilder(AutoConfiguration config) {
        this.config = config;
    }

    public static MuonBuilder withConfig(AutoConfiguration config) {
        return new MuonBuilder(config);
    }

    public Muon build() {
        try {
            return new SingleTransportMuon(config, generateDiscovery(), generateTransport());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new MuonException("Unable to create Muon instance, error during construction", e);
        }
    }

    private MuonTransport generateTransport() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String[] factoryImpl = config.getStringConfig("muon.transport.factories").split(",");

        MuonTransport transport = null;

        for(String factory: factoryImpl) {
            try {
                MuonTransportFactory factoryInstance = (MuonTransportFactory) Class.forName(factory).newInstance();
                factoryInstance.setAutoConfiguration(config);
                transport = factoryInstance.build(config.getProperties());
            } catch (ClassNotFoundException ex) {
                LOG.info("Configured transport " + factory + " not present in the classpath, ignoring");
            }
        }

        return transport;
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
