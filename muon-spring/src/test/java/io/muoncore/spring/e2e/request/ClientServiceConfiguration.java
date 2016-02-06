package io.muoncore.spring.e2e.request;

import io.muoncore.spring.annotations.EnableMuon;
import io.muoncore.spring.annotations.EnableMuonRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@EnableMuon(serviceName = "${muon.client.name}",
        tags = {"${muon.client.tag1}", "${muon.client.tag2}"})
@EnableMuonRepositories(basePackages = {"io.muoncore.spring.model.request"})
@PropertySource("classpath:application.properties")
public class ClientServiceConfiguration {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
