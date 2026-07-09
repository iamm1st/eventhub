package com.eventhub.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAction {

    String action();

    String entityType();

    // which method argument should be taken the id from
    int entityIdArgIndex() default -1;

    // is it possible to take the id from response DTO using getId()
    boolean useReturnedId() default true;
}