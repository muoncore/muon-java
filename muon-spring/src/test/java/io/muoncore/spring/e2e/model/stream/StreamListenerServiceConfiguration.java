package io.muoncore.spring.e2e.model.stream;

import io.muoncore.spring.annotations.EnableMuon;
import io.muoncore.spring.annotations.EnableMuonControllers;
import io.muoncore.spring.annotations.EnableMuonRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@EnableMuon(serviceName = "${muon.streamListener.name}",
        aesEncryptionKey = "${muon.aesEncryptionKey}")
@ComponentScan(basePackages = {"io.muoncore.spring.e2e.model.stream"})
@PropertySource("classpath:application.properties")
@EnableMuonControllers(streamKeepAliveTimeout = 100)
public class StreamListenerServiceConfiguration {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
