package io.muoncore.spring.boot;

import io.muoncore.spring.annotations.EnableMuonControllers;
import io.muoncore.spring.repository.DefaultMuonEventStoreRepository;
import io.muoncore.spring.repository.MuonEventStoreRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(MuonConfigurationProperties.class)
@EnableMuonControllers(streamKeepAliveTimeout = 100)
@PropertySource(value="classpath:application.properties", ignoreResourceNotFound=true)
public class MuonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MuonEventStoreRepository.class)
    MuonEventStoreRepository muonEventStore(ApplicationContext applicationContext) {
        MuonEventStoreRepository repo = new DefaultMuonEventStoreRepository();
        return repo;
    }

}
