package io.muoncore;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.discovery.DiscoveryFactory;
import io.muoncore.discovery.MultiDiscovery;
import io.muoncore.exception.MuonException;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.MuonTransportFactory;

import java.util.ArrayList;
import java.util.List;

public class MuonBuilder {

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
            MuonTransportFactory factoryInstance = (MuonTransportFactory) Class.forName(factory).newInstance();
            factoryInstance.setAutoConfiguration(config);
            transport = factoryInstance.build(config.getProperties());
        }

        return transport;
    }

    private Discovery generateDiscovery() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String[] factoryImpl = config.getStringConfig("muon.discovery.factories").split(",");

        List<Discovery> discoveries = new ArrayList<>();

        for(String factory: factoryImpl) {
            DiscoveryFactory factoryInstance = (DiscoveryFactory) Class.forName(factory).newInstance();
            factoryInstance.setAutoConfiguration(config);
            discoveries.add(factoryInstance.build(config.getProperties()));
        }

        return new MultiDiscovery(discoveries);
    }
}
