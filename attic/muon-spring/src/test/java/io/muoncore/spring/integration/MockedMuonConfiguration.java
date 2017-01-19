package io.muoncore.spring.integration;

import io.muoncore.Muon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import static org.mockito.Mockito.mock;

@Configuration
@PropertySource("classpath:application.properties")
public class MockedMuonConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public Muon muon() {
        return mock(Muon.class);
    }

}
