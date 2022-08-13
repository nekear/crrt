package com.github.DiachenkoMD.entities.enums;

import java.io.Serializable;
import java.util.HashMap;

public enum Roles implements Serializable, DBCoupled {
    ANY(0, "any"),
    CLIENT(1, "client"),
    DRIVER(2, "driver"),
    MANAGER(3, "manager"),
    ADMIN(4, "admin");

    private final int id;
    private final String keyword;

    Roles(int id, String keyword){
        this.id = id;
        this.keyword = keyword;
    }

    public int id(){
        return this.id;
    }

    public String keyword(){
        return this.keyword;
    }

    private final static HashMap<Integer, Roles> cachedById = new HashMap<>();

    static {
        for(Roles role : Roles.values()) {
            cachedById.put(role.id(), role);
        }
    }

    public static Roles getById(int id){
        return cachedById.get(id);
    }
}
