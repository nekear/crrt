package com.github.DiachenkoMD.entities.dto.invoices;

import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.enums.Cities;
import com.github.DiachenkoMD.entities.enums.InvoiceStatuses;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.github.DiachenkoMD.entities.DB_Constants.*;

public class DriverInvoice extends LimitedInvoice{
    private String brand;
    private String model;

    private BigDecimal salary;
    private Cities city;

    public DriverInvoice(){}

    /**
     * Constructor for getting DriverInvoice instance from ResultSet. Filly almost all fields except {@link LimitedInvoice#code} + calculates salary field inside, taking driver`s percentage from app.properties.
     * @param rs
     * @throws SQLException
     */
    protected DriverInvoice(ResultSet rs) throws SQLException{

        int id = rs.getInt("invoice_id");

        LocalDate start = rs.getDate(TBL_INVOICES_DATE_START).toLocalDate();
        LocalDate end = rs.getDate(TBL_INVOICES_DATE_END).toLocalDate();
        DatesRange dr = new DatesRange(start, end);

        List<InvoiceStatuses> invoiceStatuses = new ArrayList<>();

        if(rs.getInt(TBL_INVOICES_IS_CANCELED) == 1)
            invoiceStatuses.add(InvoiceStatuses.CANCELED);

        if(rs.getInt(TBL_INVOICES_IS_REJECTED) == 1)
            invoiceStatuses.add(InvoiceStatuses.REJECTED);

        this.setId(id);
        this.setDatesRange(dr);
        this.setStatusList(invoiceStatuses);

        ResourceBundle rb = ResourceBundle.getBundle("app"); // for getting driver salary percentage setting

        this.brand = rs.getString(TBL_CARS_BRAND);
        this.model = rs.getString(TBL_CARS_MODEL);
        this.salary = rs.getBigDecimal(TBL_INVOICES_EXP_PRICE)
                .multiply(
                        BigDecimal.valueOf(Integer.parseInt(rb.getString("driver.salary.percentage")))
                )
                .divide(
                        BigDecimal.valueOf(100)
                );

        this.city = Cities.getById(rs.getInt(TBL_CARS_CITY_ID));
    }

    public static DriverInvoice of(ResultSet rs) throws SQLException {
        return new DriverInvoice(rs);
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

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Cities getCity() {
        return city;
    }

    public void setCity(Cities city) {
        this.city = city;
    }
}
