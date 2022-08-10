package com.github.DiachenkoMD.entities.enums;

import java.util.HashMap;

public enum CarSegments implements DBCoupled{
    D_SEGMENT(1, "d_segment"),
    F_SEGMENT(2, "f_segment"),
    S_SEGMENT(3, "s_segment");

    private final int id;
    private final String keyword;

    CarSegments(int id, String keyword){
        this.id = id;
        this.keyword = keyword;
    }

    public int id(){
        return this.id;
    }

    public String keyword(){
        return this.keyword;
    }

    private final static HashMap<Integer, CarSegments> cachedById = new HashMap<>();

    static {
        for(CarSegments segment : CarSegments.values()) {
            cachedById.put(segment.id(), segment);
        }
    }

    public static CarSegments getById(int id){
        return cachedById.get(id);
    }
}
