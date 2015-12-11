package io.muoncore.spring;

import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.SingleTransportMuon;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.discovery.MultiDiscovery;
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
import java.util.List;

@Configuration
public class MuonConfiguration implements ApplicationContextAware{

    @Autowired
    private AutoConfiguration muonAutoConfiguration;

    @Autowired
    Environment env;

    @Autowired
    private List<MuonTransport> muonTransports;

    @Autowired
    private List<Discovery> muonDiscoveries;
    private ApplicationContext applicationContext;

    @Bean
    public Muon muon() throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException {
        return new SingleTransportMuon(muonAutoConfiguration,
                new MultiDiscovery(muonDiscoveries),
                getFirstValidMuonTransport(muonTransports));

    }

    //TODO Remove this method when muon multiple transports is implemented
    private MuonTransport getFirstValidMuonTransport(List<MuonTransport> muonTransports) {
        if (muonTransports == null || muonTransports.size() == 0) {
            throw new IllegalStateException("No muon transports found");
        }
        return muonTransports.get(0);
    }

    private Discovery getDiscovery(List<Discovery> muonDiscoveries) {
        if (muonDiscoveries == null || muonDiscoveries.size() ==0) {
            throw new IllegalStateException("No muon discoveries found");
        }

        return new MultiDiscovery(muonDiscoveries);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
