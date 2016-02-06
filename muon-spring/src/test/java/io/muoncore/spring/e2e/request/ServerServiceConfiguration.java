package io.muoncore.spring.e2e.request;

import io.muoncore.spring.annotations.EnableMuon;
import io.muoncore.spring.annotations.EnableMuonControllers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = {"io.muoncore.spring.model.request"})
@EnableMuon(serviceName = "${muon.server.name}",
        tags = {"${muon.server.tag1}", "${muon.server.tag2}"})
@PropertySource("classpath:application.properties")
@EnableMuonControllers
public class ServerServiceConfiguration {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
