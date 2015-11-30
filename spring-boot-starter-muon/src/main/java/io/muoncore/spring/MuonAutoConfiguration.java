package io.muoncore.spring;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MuonConfigurationProperties.class)
public class MuonAutoConfiguration {

//    @Bean
//    @ConditionalOnMissingBean(CamelContext.class)
//    CamelContext camelContext(ApplicationContext applicationContext,
//                              CamelConfigurationProperties configurationProperties) {
//        CamelContext camelContext = new SpringCamelContext(applicationContext);
//        SpringCamelContext.setNoStart(true);
//
//        if (!configurationProperties.isJmxEnabled()) {
//            camelContext.disableJMX();
//        }
//
//        if (configurationProperties.getName() != null) {
//            ((SpringCamelContext) camelContext).setName(configurationProperties.getName());
//        }
//
//        return camelContext;
//    }

}
