package com.github.DiachenkoMD.entities.enums;

/**
 * Interface, which was mainly designed to be used as marker. Needed for custom {@link com.github.DiachenkoMD.entities.adapters.DBCoupledAdapter GSON adapter} to transform enum values to id`s and backwards.
 */
public interface DBCoupled {
    int id();
    String keyword();
}
