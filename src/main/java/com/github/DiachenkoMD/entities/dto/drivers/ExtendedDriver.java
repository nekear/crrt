package com.github.DiachenkoMD.entities.dto.drivers;

import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.adapters.CryptoAdapter;
import com.github.DiachenkoMD.entities.enums.Cities;
import com.google.gson.annotations.JsonAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExtendedDriver extends LimitedDriver{
    @JsonAdapter(CryptoAdapter.class)
    private Object id;

    private Cities city;

    protected ExtendedDriver(ResultSet rs) throws SQLException {
        super(rs);
        this.id = rs.getInt(DB_Constants.TBL_DRIVERS_ID);
        this.city = Cities.getById(rs.getInt(DB_Constants.TBL_DRIVERS_CITY_ID));
    }

    public ExtendedDriver(){}

    public static ExtendedDriver of(ResultSet rs) throws SQLException{
        return new ExtendedDriver(rs);
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Cities getCity() {
        return city;
    }

    public void setCity(Cities city) {
        this.city = city;
    }
}
