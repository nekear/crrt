package com.github.DiachenkoMD.entities.dto.invoices;

import com.github.DiachenkoMD.entities.enums.Cities;
import static com.github.DiachenkoMD.entities.DB_Constants.*;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientInvoice extends LimitedInvoice{
    private String brand;
    private String model;
    private BigDecimal price;
    private Cities city;

    public ClientInvoice(){}

    protected ClientInvoice(ResultSet rs) throws SQLException{
        super(rs);

        this.brand = rs.getString(TBL_CARS_BRAND);
        this.model = rs.getString(TBL_CARS_MODEL);
        this.price = rs.getBigDecimal(TBL_INVOICES_EXP_PRICE);
        this.city = Cities.getById(rs.getInt(TBL_CARS_CITY_ID));
    }

    public static ClientInvoice of(ResultSet rs) throws SQLException {
        return new ClientInvoice(rs);
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Cities getCity() {
        return city;
    }

    public void setCity(Cities city) {
        this.city = city;
    }
}
