package com.github.DiachenkoMD.entities.dto.invoices;

import com.github.DiachenkoMD.entities.adapters.CryptoAdapter;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.google.gson.annotations.JsonAdapter;

import java.time.Period;

/**
 * Class, to transmit information about a new rental order.
 */
public class NewRent {
    @JsonAdapter(CryptoAdapter.class)
    private Object carId;

    private DatesRange datesRange;

    private Passport passport;

    private boolean isWithDriver;

    public Object getCarId() {
        return carId;
    }

    public void setCarId(Object carId) {
        this.carId = carId;
    }

    public DatesRange getDatesRange() {
        return datesRange;
    }

    public void setDatesRange(DatesRange datesRange) {
        this.datesRange = datesRange;
    }

    public Passport getPassport() {
        return passport;
    }

    public void setPassport(Passport passport) {
        this.passport = passport;
    }

    public boolean isWithDriver() {
        return isWithDriver;
    }

    public void setWithDriver(boolean withDriver) {
        isWithDriver = withDriver;
    }


}
