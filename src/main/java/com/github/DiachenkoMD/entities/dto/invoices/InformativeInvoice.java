package com.github.DiachenkoMD.entities.dto.invoices;

import com.github.DiachenkoMD.entities.adapters.Skip;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.drivers.LimitedDriver;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.github.DiachenkoMD.entities.enums.Cities;
import com.github.DiachenkoMD.entities.enums.InvoiceStatuses;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.github.DiachenkoMD.entities.DB_Constants.TBL_INVOICES_IS_CANCELED;
import static com.github.DiachenkoMD.entities.DB_Constants.TBL_INVOICES_IS_REJECTED;

/**
 * Class designed for storing full invoice info, which intended to be shown when admin or manager wants to get detailed info about specific invoice.
 */
public class InformativeInvoice extends PanelInvoice {

    private Passport passport;
    private Cities city;

    private String rejectionReason;

    private List<RepairInvoice> repairInvoices; // should be set externally

    private LocalDateTime tsCreated;

    public static InformativeInvoice of(ResultSet rs) throws SQLException{
        InformativeInvoice invoice = new InformativeInvoice();
        invoice.setId(rs.getInt(1));
        invoice.setCode(rs.getString("invoice_code"));

        DatesRange datesRange = new DatesRange();
        datesRange.setStart(rs.getDate("date_start").toLocalDate());
        datesRange.setEnd(rs.getDate("date_end").toLocalDate());
        invoice.setDatesRange(datesRange);

        invoice.setPrice(rs.getBigDecimal("exp_price"));
        invoice.setCity(Cities.getById(rs.getInt("city_id")));

        invoice.setRejectionReason(rs.getString("reject_reason"));

        List<InvoiceStatuses> statuses = new ArrayList<>();
        if(rs.getInt(TBL_INVOICES_IS_CANCELED) == 1)
            statuses.add(InvoiceStatuses.CANCELED);

        if(rs.getInt(TBL_INVOICES_IS_REJECTED) == 1)
            statuses.add(InvoiceStatuses.REJECTED);
        invoice.setStatusList(statuses);

        rs.getObject("driver_id");
        if(!rs.wasNull())
            invoice.setDriver(LimitedDriver.of(rs));

        invoice.setClientEmail(rs.getString("client_email"));
        invoice.setBrand(rs.getString("brand"));
        invoice.setModel(rs.getString("model"));

        invoice.setTsCreated(rs.getObject("ts_created", LocalDateTime.class));

        Passport passport = Passport.of(rs);

        invoice.setPassport(passport);

        return invoice;
    }


    public Passport getPassport() {
        return passport;
    }

    public void setPassport(Passport passport) {
        this.passport = passport;
    }

    public List<RepairInvoice> getRepairInvoices() {
        return repairInvoices;
    }

    public void setRepairInvoices(List<RepairInvoice> repairInvoices) {
        this.repairInvoices = repairInvoices;
    }

    public Cities getCity() {
        return city;
    }

    public void setCity(Cities city) {
        this.city = city;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getTsCreated() {
        return tsCreated;
    }

    public void setTsCreated(LocalDateTime tsCreated) {
        this.tsCreated = tsCreated;
    }
}
