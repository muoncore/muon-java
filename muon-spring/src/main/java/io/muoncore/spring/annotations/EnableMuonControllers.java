package io.muoncore.spring.annotations;

import io.muoncore.spring.MuonControllersConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Triggers processing of muon listener annotations:
 * <ul>
 * <li>{@link io.muoncore.spring.annotations.MuonController}</li>
 * <li>{@link io.muoncore.spring.annotations.MuonQueryListener}</li>
 * <li>{@link io.muoncore.spring.annotations.MuonStreamListener}</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({MuonControllersConfiguration.class})
public @interface EnableMuonControllers {
}
