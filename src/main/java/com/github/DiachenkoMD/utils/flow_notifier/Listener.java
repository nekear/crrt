package com.github.DiachenkoMD.utils.flow_notifier;

@FunctionalInterface
public interface Listener <T> {
    void ping(T o);
}
