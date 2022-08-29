package com.github.DiachenkoMD.web.utils.middlewares.warden;

import com.github.DiachenkoMD.web.utils.middlewares.origins.Ward;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseWards {
    Class<? extends Ward>[] value();
}

