package com.github.DiachenkoMD.entities.dto.invoices;

import static com.github.DiachenkoMD.entities.DB_Constants.*;
import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.enums.InvoiceStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Invoice {
    private Object id;
    private String code;
    private Car car;
    private DatesRange dates;
    private List<InvoiceStatus> statusList;

    public static Invoice of(ResultSet rs) throws SQLException {
        int id = rs.getInt("invoice_id");
        String code = rs.getString(TBL_INVOICES_CODE);
        Car car = Car.of(rs);


        LocalDate start = rs.getDate(TBL_INVOICES_DATE_START).toLocalDate();
        LocalDate end = rs.getDate(TBL_INVOICES_DATE_END).toLocalDate();
        DatesRange dr = new DatesRange(start, end);

        List<InvoiceStatus> invoiceStatuses = new ArrayList<>();

        if(rs.getInt(TBL_INVOICES_IS_CANCELED) == 1)
            invoiceStatuses.add(InvoiceStatus.CANCELED);

        if(rs.getInt(TBL_INVOICES_IS_REJECTED) == 1)
            invoiceStatuses.add(InvoiceStatus.REJECTED);

        if(rs.getInt(TBL_INVOICES_IS_PAID) == 1)
            invoiceStatuses.add(InvoiceStatus.PAID);

        Invoice invoice = new Invoice();

        invoice.setId(id);
        invoice.setCode(code);
        invoice.setCar(car);
        invoice.setDates(dr);
        invoice.setStatusList(invoiceStatuses);

        return invoice;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public DatesRange getDates() {
        return dates;
    }

    public void setDates(DatesRange dates) {
        this.dates = dates;
    }

    public List<InvoiceStatus> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<InvoiceStatus> statusList) {
        this.statusList = statusList;
    }
}
