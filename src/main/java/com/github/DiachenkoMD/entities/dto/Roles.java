package com.github.DiachenkoMD.entities.dto;

public enum Roles {
    ANY(0),
    DEFAULT(1),
    DRIVER(2),
    MANAGER(3),
    ADMIN(4);

    final int ordinal;

    Roles(int ord){
        this.ordinal = ord;
    }

    public static Roles byIndex(int i){
        for(Roles role : Roles.values()){
            if(role.ordinal == i)
                return role;
        }

        return null;
    }
}
