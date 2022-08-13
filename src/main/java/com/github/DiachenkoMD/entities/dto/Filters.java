package com.github.DiachenkoMD.entities.dto;

import java.util.HashMap;

public abstract class Filters {
    /**
     * Method for obtaining clean and db query ready params to use in LIKE, for example.
     * @return HashMap where key is column name in corresponding !query! and value is parameter to search by.
     */
    public abstract HashMap<String, String> getDBPresentation();
}
