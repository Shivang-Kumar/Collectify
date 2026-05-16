package edu.tcu.cs.hogwarts_artifacts_online.observability.tracing;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Traced {
   String value() default"";
}
