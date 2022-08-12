package com.github.DiachenkoMD.entities.dto.invoices;

import com.github.DiachenkoMD.entities.dto.drivers.LimitedDriver;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PanelInvoice extends LimitedInvoice{
    private String brand;
    private String model;
    private LimitedDriver driver;
    private String clientEmail;
    private BigDecimal price;

    protected PanelInvoice(ResultSet rs) throws SQLException{
        super(rs);
        System.out.println("RS ON PanelInvoice");
        this.brand = rs.getString("brand");
        this.model = rs.getString("model");

        rs.getObject("driver_id");
        if(!rs.wasNull())
            this.driver = LimitedDriver.of(rs);

        this.clientEmail = rs.getString("client_email");
        this.price = rs.getBigDecimal("exp_price");
    }

    public static PanelInvoice of(ResultSet rs) throws SQLException{
        return new PanelInvoice(rs);
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

    public LimitedDriver getDriver() {
        return driver;
    }

    public void setDriver(LimitedDriver driver) {
        this.driver = driver;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("PANEL_INVOICE:{%s, %s, %s, %s, %s}", getId(), getCode(), getModel() + " " + getBrand(), getClientEmail(), getStatusList());
    }
}
