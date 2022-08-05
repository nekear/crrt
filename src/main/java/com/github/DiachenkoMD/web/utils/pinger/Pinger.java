package com.github.DiachenkoMD.web.utils.pinger;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides listener pattern implementation for, for example, logging events + can be used for catching exceptions in testing when they are in try/catch blocks. <br/>
 * Use {@link #addListener(Class, Listener) addListener(Class, Listener)} to add listener and bind its invocation when Class is passed into {@link #omit(Object) omit(Object)} method (triggering is done by 'instance of' logic).<br/>
 * Use {@link #omit(Object) omit(Object)} to trigger listeners (as I mentioned earlier, triggering is done by 'instance of' logic).
 */
public class Pinger {
    private final LinkedHashMap<Class<?>, Listener<?>> listeners;

    public Pinger(){
        this.listeners = new LinkedHashMap<>();
    }

    /**
     * Triggers events on relevant listeners if caller is instance of binded listener class.
     * @param caller - event trigger.
     * @param <T> - can be anything.
     */
    @SuppressWarnings("unchecked")
    public <T> void omit(T caller){
        for(Map.Entry<Class<?>, Listener<?>> entry : listeners.entrySet()){
            if(caller.getClass().isAssignableFrom(entry.getKey())){
                Listener<T> listener = (Listener<T>) entry.getValue();
                listener.ping(caller);
            }
        }
    }

    /**
     * Adds listeners to the list
     * @param clazz - class which matching with caller.class (or being its parent) at {@link #omit(Object) omit(Object)} will trigger event.
     * @param listener - event which will be triggered.
     * @param <T> - can be anything.
     */
    public <T> void addListener(Class<T> clazz, Listener<T> listener){
        listeners.put(clazz, listener);
    }

}
