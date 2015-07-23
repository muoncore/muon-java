package io.muoncore.spring.annotations.parameterhandlers;

import java.lang.annotation.*;

/**
 * Parameter, annotated by this annotation, would get a PhotonEvent payload
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PhotonPayload {
}
