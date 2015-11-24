package io.muoncore.spring.e2e.model.stream;

import io.muoncore.spring.annotations.EnableMuon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@EnableMuon(serviceName = "${muon.streamSource.name}",
        aesEncryptionKey = "${muon.aesEncryptionKey}")
@PropertySource("classpath:application.properties")
public class StreamSourceServiceConfiguration {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
