package com.github.DiachenkoMD.utils.flow_notifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class FlowNotifier {
    private final LinkedHashMap<Class<?>, Listener<?>> listeners;

    public FlowNotifier(){
        this.listeners = new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T extends Exception> void omit_e(T exc){
        System.out.println(listeners);

        for(Map.Entry<Class<?>, Listener<?>> entry : listeners.entrySet()){
            if(entry.getKey() == exc.getClass()){
                Listener<T> listener = (Listener<T>) entry.getValue();
                listener.ping(exc);
            }
        }
    }

    public <T> void addListener(Class<T> clazz, Listener<T> listener){
        listeners.put(clazz, listener);
    }

    private final class GenericResolver<T>{
        private GenericResolver(){}
    }
}
