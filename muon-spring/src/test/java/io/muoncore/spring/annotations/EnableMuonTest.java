package io.muoncore.spring.annotations;

import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.MuonExtension;
import io.muoncore.spring.MuonControllerBeanPostProcessor;
import io.muoncore.spring.mapping.MuonResourceService;
import io.muoncore.spring.mapping.MuonStreamSubscriptionService;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class EnableMuonTest {
    @Test
    public void shouldCreateMuonBeans() throws Exception {
        ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(
                SampleEnableMuonConfiguration.class);
        assertThat(ctx.getBean(Muon.class), notNullValue());
        assertThat(ctx.getBean(MuonControllerBeanPostProcessor.class), notNullValue());
        assertThat(ctx.getBean(MuonStreamSubscriptionService.class), notNullValue());
        assertThat(ctx.getBean(MuonResourceService.class), notNullValue());
    }

    @Configuration
    @EnableMuon(serviceName = "test-service-name")
    static class SampleEnableMuonConfiguration {

        @Bean
        Discovery muonDiscovery() {
            return mock(Discovery.class);
        }

        @Bean
        MuonExtension amqpMuonExtension() {
            return mock(MuonExtension.class);
        }

    }
}