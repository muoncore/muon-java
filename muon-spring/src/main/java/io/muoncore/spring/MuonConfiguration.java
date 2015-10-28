package io.muoncore.spring;

import io.muoncore.Muon;
import io.muoncore.config.MuonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;

@Configuration
public class MuonConfiguration {
    @Autowired
    private MuonConfigurationHolder muonConfigurationHolder;

    @Bean
    public Muon muon() throws URISyntaxException, InterruptedException {
        MuonBuilder.addWriter(new SpringBasedConfigurationWriter(muonConfigurationHolder.getDiscoveryUrl()));
        MuonBuilder muonBuilder = new MuonBuilder()
                .withServiceIdentifier(muonConfigurationHolder.getServiceName());
        if (muonConfigurationHolder.getTags() != null) {
            muonBuilder.withTags(muonConfigurationHolder.getTags());
        }
        final Muon muon = muonBuilder
                .build();

        muon.start();

        //TODO Move this to a lifecycle event
        Thread.sleep(6000);

        return muon;
    }
}
