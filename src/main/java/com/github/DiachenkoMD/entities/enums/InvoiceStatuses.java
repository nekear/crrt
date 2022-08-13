package com.github.DiachenkoMD.entities.enums;

import java.util.HashMap;

public enum InvoiceStatuses implements DBCoupled{
    ANY(0, "any"),
    ALIVE(1, "alive"),
    REJECTED(2, "rejected"),
    CANCELED(3, "cancelled"),
    ACTIVE_REPAIRS(4, "active_repairs"),
    EXPIRED_REPAIRS(5, "expired_repairs");

    private final int id;
    private final String keyword;

    InvoiceStatuses(int id, String keyword){
        this.id = id;
        this.keyword = keyword;
    }

    public int id(){
        return this.id;
    }

    public String keyword(){
        return this.keyword;
    }

    private static HashMap<Integer, InvoiceStatuses> cachedById = new HashMap<>();

    static {
        for(InvoiceStatuses status : InvoiceStatuses.values()) {
            cachedById.put(status.id(), status);
        }
    }

    public static InvoiceStatuses getById(int id){
        return cachedById.get(id);
    }
}
