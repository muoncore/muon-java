package io.muoncore.spring.annotations.parameterhandlers;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Parameter {
    /**
     * @return Muon parameter stream
     */
    String value();
}
