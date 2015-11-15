package io.muoncore.spring.annotations;

import io.muoncore.Discovery;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonBuilder;
import io.muoncore.spring.MuonControllerBeanPostProcessor;
import io.muoncore.spring.mapping.MuonRequestListenerService;
import io.muoncore.spring.mapping.MuonStreamSubscriptionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class EnableMuonTest {

    @Mock
    private MuonBuilder.DiscoveryBuilder discoveryBuilder;

/*    @Mock
    private MuonBuilder.ExtensionBuilder muonExtensionBuilder;

    @Mock
    private Discovery mockedDiscovery;

    @Mock
    private MuonExtension mockedExtension;

    static {
        MuonBuilder.clearWriters();
        MuonBuilder.clearDiscovery();
        MuonBuilder.clearExtensions();
    }

    @Before
    public void setUp() throws Exception {
        MuonBuilder.registerDiscovery(discoveryBuilder);
        MuonBuilder.registerExtension(muonExtensionBuilder);
        doAnswer(invocation -> {
            AutoConfiguration config = (AutoConfiguration) invocation.getArguments()[0];
            if (config.getDiscoveryUrl().equals("amqp://the-amqp-host")) {
                return mockedDiscovery;
            } else {
                return null;
            }
        }).when(discoveryBuilder).create(Matchers.any());
        doAnswer(invocation -> {
            AutoConfiguration config = (AutoConfiguration) invocation.getArguments()[0];
            if (config.getDiscoveryUrl().equals("amqp://the-amqp-host")) {
                return mockedExtension;
            } else {
                return null;
            }
        }).when(muonExtensionBuilder).create(Matchers.any());
    }

    @Test
    public void shouldCreateMuonBeans() throws Exception {
        ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(
                SampleEnableMuonConfiguration.class);
        final OldMuon muon = ctx.getBean(OldMuon.class);
        assertThat(muon, notNullValue());
        assertThat(muon.getServiceIdentifer(), is("muon-test"));
        assertThat(muon.getTags(), equalTo(Arrays.asList("tag1Value", "tag2Value")));
        assertThat(ctx.getBean(MuonControllerBeanPostProcessor.class), notNullValue());
        assertThat(ctx.getBean(MuonStreamSubscriptionService.class), notNullValue());
        assertThat(ctx.getBean(MuonRequestListenerService.class), notNullValue());

    }

    @Configuration
    @EnableMuon(serviceName = "${muon.service.name}", tags = {"${muon.service.tag1}", "${muon.service.tag2}"}, aesEncryptionKey = "${muon.service.aesEncryptionKey}")
    @PropertySource("classpath:application.properties")
    static class SampleEnableMuonConfiguration {
        @Bean
        public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }*/
}