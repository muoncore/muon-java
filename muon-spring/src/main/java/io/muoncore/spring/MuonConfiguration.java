package io.muoncore.spring;

import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.SingleTransportMuon;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.transport.MuonTransport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class MuonConfiguration implements ApplicationContextAware{

    @Autowired
    private AutoConfiguration muonAutoConfiguration;

    @Autowired
    Environment env;

    @Autowired
    private MuonTransport[] muonTransports;

    @Autowired
    private Discovery[] muonDiscoveries;
    private ApplicationContext applicationContext;

    @Bean
    public Muon muon() throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException {
        return new SingleTransportMuon(muonAutoConfiguration, getFirstValidDiscovery(muonDiscoveries), getFirstValidMuonTransport(muonTransports));

    }

    //TODO Remove this method when muon multiple transports is implemented
    private MuonTransport getFirstValidMuonTransport(MuonTransport[] muonTransports) {
        for (MuonTransport muonTransport : muonTransports) {
            if (muonTransport != null) {
                return muonTransport;
            }
        }
        throw new IllegalStateException("No muon transports found");
    }

    //TODO Remove this method when muon multiple discoveries is implemented
    private Discovery getFirstValidDiscovery(Discovery[] muonDiscoveries) {
        for (Discovery discovery : muonDiscoveries) {
            if (discovery != null) {
                return discovery;
            }
        }
        throw new IllegalStateException("No muon discoveries found");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
