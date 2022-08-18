package com.github.DiachenkoMD.web.utils.guardian;

import com.github.DiachenkoMD.web.utils.guardian.guards.Guard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseGuards {
    Class<? extends Guard>[] value();
}
