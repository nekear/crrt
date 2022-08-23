package com.github.DiachenkoMD.entities.dto.invoices;

import static com.github.DiachenkoMD.entities.DB_Constants.*;

import com.github.DiachenkoMD.entities.adapters.CryptoAdapter;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.enums.InvoiceStatuses;
import com.google.gson.annotations.JsonAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LimitedInvoice {
    @JsonAdapter(CryptoAdapter.class)
    private Object id;
    private String code;
    private DatesRange datesRange;
    private List<InvoiceStatuses> statusList;

    public LimitedInvoice(){}

    protected LimitedInvoice(ResultSet rs) throws SQLException{
        int id = rs.getInt("invoice_id");
        String code = rs.getString("invoice_"+TBL_INVOICES_CODE);

        LocalDate start = rs.getDate(TBL_INVOICES_DATE_START).toLocalDate();
        LocalDate end = rs.getDate(TBL_INVOICES_DATE_END).toLocalDate();
        DatesRange dr = new DatesRange(start, end);

        List<InvoiceStatuses> invoiceStatuses = new ArrayList<>();

        if(rs.getInt(TBL_INVOICES_IS_CANCELED) == 1)
            invoiceStatuses.add(InvoiceStatuses.CANCELED);

        if(rs.getInt(TBL_INVOICES_IS_REJECTED) == 1)
            invoiceStatuses.add(InvoiceStatuses.REJECTED);

        if(rs.getInt("activeRepairs") > 0)
            invoiceStatuses.add(InvoiceStatuses.ACTIVE_REPAIRS);

        if(rs.getInt("expiredRepairs") > 0)
            invoiceStatuses.add(InvoiceStatuses.EXPIRED_REPAIRS);

        this.setId(id);
        this.setCode(code);
        this.setDatesRange(dr);
        this.setStatusList(invoiceStatuses);
    }

    public static LimitedInvoice of(ResultSet rs) throws SQLException {
        return new LimitedInvoice(rs);
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

    public DatesRange getDatesRange() {
        return datesRange;
    }

    public void setDatesRange(DatesRange datesRange) {
        this.datesRange = datesRange;
    }

    public List<InvoiceStatuses> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<InvoiceStatuses> statusList) {
        this.statusList = statusList;
    }

    public boolean isRejected(){
        return this.statusList.contains(InvoiceStatuses.REJECTED);
    }

    public boolean isCancelled(){
        return this.statusList.contains(InvoiceStatuses.CANCELED);
    }
}
