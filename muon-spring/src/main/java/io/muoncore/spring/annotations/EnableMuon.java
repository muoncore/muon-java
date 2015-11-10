package io.muoncore.spring.annotations;

import io.muoncore.spring.MuonConfiguration;
import io.muoncore.spring.AutoConfigurationBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable spring-muon integration. Will create an instance of Muon
 * @see io.muoncore.spring.annotations.EnableMuonControllers
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({MuonConfiguration.class, AutoConfigurationBeanDefinitionRegistrar.class})
@EnableMuonControllers
public @interface EnableMuon {

    /**
     * Assign a name to the Muon service instance
     */
    String serviceName();

    /**
     * List of optional service tags of Muon instance
     */
    String[] tags() default {};

    /**
     * Discovery url, currently only amqp://... supported
     */
    String discoveryUrl();

}
