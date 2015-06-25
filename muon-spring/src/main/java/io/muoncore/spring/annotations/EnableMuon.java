package io.muoncore.spring.annotations;

import io.muoncore.spring.MuonBootstrapConfiguration;
import io.muoncore.spring.MuonServiceNameBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable Muon annotated endpoints that are created under the cover
 * by a {@link io.muoncore.spring.MuonControllerBeanPostProcessor}
 * To be used on {@link org.springframework.context.annotation.Configuration Configuration}
 * classes.
 * Additionally, creates instance of Muon. Requires {@link io.muoncore.Discovery}
 * bean to be present in the context. Additionally will pick up all {@link io.muoncore.MuonExtension}
 * instances.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({MuonServiceNameBeanDefinitionRegistrar.class, MuonBootstrapConfiguration.class})
public @interface EnableMuon {

    /**
     * Assign a name to the Muon service instance
     */
    String serviceName();

}
