package io.muoncore.spring.boot;

import io.muoncore.spring.AutoConfigurationBeanDefinitionRegistrar;
import io.muoncore.spring.MuonConfiguration;
import io.muoncore.spring.annotations.EnableMuon;
import io.muoncore.spring.annotations.EnableMuonControllers;
import io.muoncore.spring.discovery.MuonDiscoveryFactoryBeanRegistrar;
import io.muoncore.spring.repository.DefaultMuonEventStoreRepository;
import io.muoncore.spring.repository.MuonEventStoreRepository;
import io.muoncore.spring.transport.MuonTransportFactoryBeanRegistrar;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableMuon(serviceName = "simpleService", aesEncryptionKey = "1234567890123456", tags = {"simple", "node"})
@Import({MuonTransportFactoryBeanRegistrar.class,  MuonDiscoveryFactoryBeanRegistrar.class,
        MuonConfiguration.class, AutoConfigurationBeanDefinitionRegistrar.class})
@EnableConfigurationProperties(MuonConfigurationProperties.class)
@EnableMuonControllers(streamKeepAliveTimeout = 100)
@PropertySource("classpath:application.properties")
public class MuonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MuonEventStoreRepository.class)
    MuonEventStoreRepository muonEventStore(ApplicationContext applicationContext) {
        MuonEventStoreRepository repo = new DefaultMuonEventStoreRepository();


        return repo;
    }

}
