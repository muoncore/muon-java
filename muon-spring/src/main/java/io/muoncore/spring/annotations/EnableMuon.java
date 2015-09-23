package io.muoncore.spring.annotations;

import io.muoncore.spring.MuonBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable spring-muon integration. Will create an instance of Muon
 * @see io.muoncore.spring.annotations.EnableMuonControllers
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({MuonBeanDefinitionRegistrar.class})
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
