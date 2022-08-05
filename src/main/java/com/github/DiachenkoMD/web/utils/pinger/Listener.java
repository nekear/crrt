package com.github.DiachenkoMD.web.utils.pinger;

@FunctionalInterface
public interface Listener <T> {
    void ping(T o);
}
