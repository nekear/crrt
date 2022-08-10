package com.github.DiachenkoMD.entities.enums;

import java.util.HashMap;

public enum InvoiceStatus implements DBCoupled{
    ALIVE(1, "alive"),
    PAID(2, "paid"),
    REJECTED(3, "rejected"),
    CANCELED(4, "canceled");

    private final int id;
    private final String keyword;

    InvoiceStatus(int id, String keyword){
        this.id = id;
        this.keyword = keyword;
    }

    public int id(){
        return this.id;
    }

    public String keyword(){
        return this.keyword;
    }

    private static HashMap<Integer, InvoiceStatus> cachedById = new HashMap<>();

    static {
        for(InvoiceStatus status : InvoiceStatus.values()) {
            cachedById.put(status.id(), status);
        }
    }

    public static InvoiceStatus getById(int id){
        return cachedById.get(id);
    }
}
