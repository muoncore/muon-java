package io.muoncore.spring;

import io.muoncore.spring.mapping.MuonResourceService;
import io.muoncore.spring.mapping.MuonStreamSubscriptionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MuonBootstrapConfiguration {

    @Bean
    public static MuonControllerBeanPostProcessor muonControllerBeanPostProcessor() {
        return new MuonControllerBeanPostProcessor();
    }

    @Bean
    public MuonStreamSubscriptionService muonStreamSubscriptionService() {
        return new MuonStreamSubscriptionService();
    }

    @Bean
    public MuonResourceService muonResourceService() {
        return new MuonResourceService();
    }
}

