package io.muoncore.spring;

import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.MuonExtension;
import io.muoncore.exception.MuonException;
import io.muoncore.spring.mapping.MuonResourceService;
import io.muoncore.spring.mapping.MuonStreamSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
import java.util.List;

import static io.muoncore.spring.MuonServiceNameBeanDefinitionRegistrar.MUON_ANNOTATION_CONFIG_SERVICE_NAME;

@Configuration
public class MuonBootstrapConfiguration {

    @Autowired
    @Qualifier(MUON_ANNOTATION_CONFIG_SERVICE_NAME)
    private String muonServiceName;

    @Autowired
    private Discovery discovery;

    @Autowired(required = false)
    private List<MuonExtension> muonExtensions;

    @Bean
    public static MuonControllerBeanPostProcessor muonControllerBeanPostProcessor() {
        return new MuonControllerBeanPostProcessor();
    }

    @Bean
    public Muon muon() {
        Muon muon = new Muon(discovery);
        muon.setServiceIdentifer(muonServiceName);
        for (MuonExtension muonExtension : muonExtensions) {
            muonExtension.extend(muon);
        }

        try {
            muon.start();
        } catch (URISyntaxException e) {
            throw new MuonException(e);
        }

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return muon;
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

