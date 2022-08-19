package com.github.DiachenkoMD.entities.dto.invoices;

import com.github.DiachenkoMD.entities.adapters.CryptoAdapter;
import com.github.DiachenkoMD.entities.adapters.Skip;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RepairInvoice {
    @JsonAdapter(CryptoAdapter.class)
    private Object id;

    private BigDecimal price;

    private LocalDate expirationDate;

    private String comment;

    private boolean isPaid;

    private LocalDateTime tsCreated;

    @Skip
    private String clientEmail; // for informing client about delete his repairment invoice (should not be exposed to client side)

    @Skip
    private int originInvoiceId;

    public static RepairInvoice of(ResultSet rs) throws SQLException{
        RepairInvoice invoice = new RepairInvoice();
        invoice.setId(rs.getInt("id"));
        invoice.setPrice(rs.getBigDecimal("price"));
        invoice.setExpirationDate(rs.getDate("expiration_date").toLocalDate());
        invoice.setComment(rs.getString("comment"));
        invoice.setPaid(rs.getInt("is_paid") == 1);
        invoice.setTsCreated(rs.getTimestamp("ts_created").toLocalDateTime());
        invoice.setOriginInvoiceId(rs.getInt("invoice_id"));
        return invoice;
    }



    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public LocalDateTime getTsCreated() {
        return tsCreated;
    }

    public void setTsCreated(LocalDateTime tsCreated) {
        this.tsCreated = tsCreated;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public int getOriginInvoiceId() {
        return originInvoiceId;
    }

    public void setOriginInvoiceId(int originInvoiceId) {
        this.originInvoiceId = originInvoiceId;
    }

    @Override
    public String toString() {
        return "RepairInvoice{" +
                "id=" + id +
                ", price=" + price +
                ", expirationDate=" + expirationDate +
                ", comment='" + comment + '\'' +
                ", isPaid=" + isPaid +
                ", tsCreated=" + tsCreated +
                ", clientEmail='" + clientEmail + '\'' +
                ", originInvoiceId=" + originInvoiceId +
                '}';
    }
}
