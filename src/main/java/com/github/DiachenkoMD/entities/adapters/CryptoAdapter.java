package com.github.DiachenkoMD.entities.adapters;

import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;

public class CryptoAdapter implements JsonSerializer<Object>, JsonDeserializer<Object> {
    private final static Logger logger = LogManager.getLogger(CryptoAdapter.class);

    @Override
    public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Object resp = null;
        logger.info(jsonElement.getAsString());
        try {
            resp = Integer.parseInt(CryptoStore.decrypt(jsonElement.getAsString()));
        }catch (DescriptiveException e){
            logger.error(e);
        }
        return resp;
    }

    @Override
    public JsonElement serialize(Object o, Type type, JsonSerializationContext jsonSerializationContext) {
        try {
            return new JsonPrimitive(CryptoStore.encrypt(o.toString()));
        }catch (DescriptiveException e){
            logger.error(e);
            return null;
        }
    }
}
