package io.muoncore.spring.e2e.stream;

import io.muoncore.spring.annotations.EnableMuon;
import io.muoncore.spring.annotations.EnableMuonControllers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@EnableMuon(serviceName = "${muon.streamListener.name}")
@ComponentScan(basePackages = {"io.muoncore.spring.model.stream"})
@PropertySource("classpath:application.properties")
@EnableMuonControllers(streamKeepAliveTimeout = 100)
public class StreamListenerServiceConfiguration {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
