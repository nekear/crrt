package com.github.DiachenkoMD.entities.enums;

import java.util.HashMap;

public enum Cities implements DBCoupled{
    KYIV(1, "kyiv"),
    LVIV(2, "lviv");

    private final int id;
    private final String keyword;

    Cities(int id, String keyword){
        this.id = id;
        this.keyword = keyword;
    }

    public int id(){
        return this.id;
    }

    public String keyword(){
        return this.keyword;
    }

    private static final HashMap<Integer, Cities> cachedById = new HashMap<>();

    static {
        for(Cities city : Cities.values()) {
            cachedById.put(city.id(), city);
        }
    }

    public static Cities getById(int id){
        return cachedById.get(id);
    }
}
