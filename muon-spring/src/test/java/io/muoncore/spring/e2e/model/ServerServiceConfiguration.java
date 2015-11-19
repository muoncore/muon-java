package io.muoncore.spring.e2e.model;

import io.muoncore.spring.annotations.EnableMuon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = {"io.muoncore.spring.e2e.model.request"})
@EnableMuon(serviceName = "${muon.server.name}",
        tags = {"${muon.server.tag1}", "${muon.server.tag2}"},
        aesEncryptionKey = "${muon.aesEncryptionKey}")
@PropertySource("classpath:application.properties")
public class ServerServiceConfiguration {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
