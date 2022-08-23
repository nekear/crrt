package com.github.DiachenkoMD.entities.enums;

public enum VisualThemes {
    DARK("dark_theme"),
    LIGHT("white_theme");

    private String fileName;

    VisualThemes(String fileName){
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
