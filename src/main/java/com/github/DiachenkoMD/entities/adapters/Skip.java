package com.github.DiachenkoMD.entities.adapters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Annotation to prevent serialization of specific fields. Registered at {@link com.github.DiachenkoMD.web.listeners.ContextListener ContextListener}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Skip {
}
