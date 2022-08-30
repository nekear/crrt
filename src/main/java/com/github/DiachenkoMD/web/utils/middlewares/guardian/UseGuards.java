package com.github.DiachenkoMD.web.utils.middlewares.guardian;

import com.github.DiachenkoMD.web.utils.middlewares.origins.Guard;
import com.github.DiachenkoMD.web.utils.middlewares.origins.Ward;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to combine {@link Guard guards} together.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseGuards {
    Class<? extends Guard>[] value();
}
