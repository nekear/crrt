package com.github.DiachenkoMD.utils;

import java.io.FileReader;
import java.util.Properties;

public class PropertiesManager {
    private static Properties props;
    static {
        reload();
    }

    public static String get(String property){
        try{
            return props.getProperty(property);
        }catch (Exception e){
            throw new IllegalArgumentException(
                    String.format(
                            "Exception on PropertiesManager.get(%s)!",
                            property
                    ),
                    e
            );
        }
    }

    public static void reload(){
        try(FileReader fr = new FileReader("app.properties")){
            props = new Properties();
            props.load(fr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
