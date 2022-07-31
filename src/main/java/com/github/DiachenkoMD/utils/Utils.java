package com.github.DiachenkoMD.utils;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

public class Utils {
    public static <T> T flatJsonParser(String json, Class<T> parsingDestination) {
        try {
            JSONObject parsedJsonObject = new JSONObject(json);

            Iterator<String> it = parsedJsonObject.keys();

            T parsingDestInstance = parsingDestination.getConstructor().newInstance();

            int failedMethodsToInvoke = 0;

            while (it.hasNext()) {
                String key = it.next();
                Object value = parsedJsonObject.get(key);

                Class<?> classType;

                if (value instanceof Boolean) {
                    classType = Boolean.class;
                } else if (value instanceof Number) {
                    classType = Number.class;
                } else {
                    classType = String.class;
                }

                try{
                    Method settingMethod = parsingDestination.getDeclaredMethod("set" + (capitalize(key)), classType);

                    settingMethod.invoke(parsingDestInstance, value);
                }catch (NoSuchMethodException | SecurityException ignored){
                    ++failedMethodsToInvoke;
                }

            }

            return failedMethodsToInvoke == parsedJsonObject.length() ? null : parsingDestInstance;
        }catch (NoSuchMethodException| InvocationTargetException | IllegalAccessException | InstantiationException e){
            throw new IllegalStateException(String.format("[FJP] -> [Dest: %s] Exception occured while parsing %s", parsingDestination.getName(), json), e);
        }
    }

    public static String capitalize(String incoming){
        if(incoming != null){
            return incoming.substring(0,1).toUpperCase() + incoming.substring(1);
        }else{
            throw new IllegalArgumentException("Unable to capitalize *null*!");
        }
    }

    public static <T> boolean reflectiveEquals(T obj1, T obj2){
        Field[] obj1Fields = obj1.getClass().getDeclaredFields();
        Field[] obj2Fields = obj2.getClass().getDeclaredFields();

        try{
            for(int i = 0; i < obj1Fields.length; i++){
                Class<?> currentType = obj1Fields[i].getType();

                Field f1 = obj1Fields[i];
                Field f2 = obj2Fields[i];

                f1.setAccessible(true);
                f2.setAccessible(true);

                if(f1.get(obj1) != null && f2.get(obj2) != null) {
                    if (currentType.equals(String.class) && !((String) f1.get(obj1)).equalsIgnoreCase((String) f2.get(obj2)))
                        return false;
                    else if (currentType.equals(Number.class) && f1.getDouble(obj1) != f2.getDouble(obj2))
                        return false;
                    else if (currentType.equals(Boolean.class) && f1.getBoolean(obj1) != f2.getBoolean(obj2))
                        return false;
                    else if (!f1.get(obj1).equals(f2.get(obj2)))
                        return false;
                }else if(f1.get(obj1) == null && f2.get(obj2) != null || f1.get(obj2) == null && f2.get(obj1) != null) {
                    return false;
                }
            }

            return true;
        }catch (IllegalAccessException e){
            throw new IllegalStateException(String.format("[RE] Exception caught while comparing %s and %s", obj1, obj2), e);
        }
    }

}
