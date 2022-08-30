package com.github.DiachenkoMD.entities.dto;

/**
 * Designed to store data about specified order filter.
 */
public class Ordery {
    private String name;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s]", this.name, this.type);
    }
}
