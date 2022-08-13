package com.github.DiachenkoMD.entities.enums;

import java.io.Serializable;
import java.util.HashMap;

public enum AccountStates implements Serializable, DBCoupled {
    ANY(0, "any"), // MUSTN`T BE SET UP ON SERVER SIDE. FOR CLIENT USE ONLY
    BLOCKED(1, "blocked"),
    UNBLOCKED(2, "unblocked");
    private final int id;
    private final String keyword;

    AccountStates(int id, String keyword){
        this.id = id;
        this.keyword = keyword;
    }

    public int id(){
        return this.id;
    }

    public String keyword(){
        return this.keyword;
    }

    private final static HashMap<Integer, AccountStates> cachedById = new HashMap<>();

    static {
        for(AccountStates state : AccountStates.values()) {
            cachedById.put(state.id(), state);
        }
    }

    public static AccountStates getById(int id){
        return cachedById.get(id);
    }
}
