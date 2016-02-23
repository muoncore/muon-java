package io.muoncore.spring;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class MuonConfiguration {

    @Autowired
    private AutoConfiguration muonAutoConfiguration;

    @Bean
    public Muon muon() throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException {
        return MuonBuilder.withConfig(muonAutoConfiguration).build();
    }
}
