package com.github.DiachenkoMD.entities.dto;

import com.github.DiachenkoMD.entities.dto.invoices.InvoicePanelFilters;
import com.github.DiachenkoMD.entities.dto.users.UsersPanelFilters;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;

public class PaginationRequest {
    private int askedPage;
    private int elementsPerPage;

    @SerializedName("usersFilters")
    private UsersPanelFilters usersFilters;

    @SerializedName("invoicesFilters")
    private InvoicePanelFilters invoicesFilters;

    public int getAskedPage() {
        return askedPage;
    }

    public void setAskedPage(int askedPage) {
        this.askedPage = askedPage;
    }

    public int getElementsPerPage() {
        return elementsPerPage;
    }

    public void setElementsPerPage(int elementsPerPage) {
        this.elementsPerPage = elementsPerPage;
    }

    public UsersPanelFilters getUsersFilters() {
        return usersFilters;
    }

    public void setUsersFilters(UsersPanelFilters usersFilters) {
        this.usersFilters = usersFilters;
    }

    public InvoicePanelFilters getInvoicesFilters() {
        return invoicesFilters;
    }

    public void setInvoicesFilters(InvoicePanelFilters invoicesFilters) {
        this.invoicesFilters = invoicesFilters;
    }



    @Override
    public String toString() {
        Filters current = null;

        if(usersFilters != null)
            current = this.getUsersFilters();

        if(invoicesFilters != null)
            current = this.getInvoicesFilters();

        return String.format("{\n" +
                "   AskedPage: %d,\n" +
                "   ElementsPerPage: %d,\n" +
                "   Filters: %s,\n" +
                "}", this.getAskedPage(), this.getElementsPerPage(), current);
    }
}
