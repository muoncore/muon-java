package io.muoncore.spring.annotations;

import io.muoncore.spring.MuonBeanDefinitionRegistrar;
import io.muoncore.spring.MuonBootstrapConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable spring-muon integration. Will create an instance of Muon
 * Triggers processing of muon litener annotations:
 * <ul>
 * <li>{@link io.muoncore.spring.annotations.MuonController}</li>
 * <li>{@link io.muoncore.spring.annotations.MuonQueryListener}</li>
 * <li>{@link io.muoncore.spring.annotations.MuonStreamListener}</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({MuonBeanDefinitionRegistrar.class, MuonBootstrapConfiguration.class})
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
