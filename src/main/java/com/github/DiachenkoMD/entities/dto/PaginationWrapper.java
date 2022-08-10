package com.github.DiachenkoMD.entities.dto;

import com.google.gson.annotations.SerializedName;

public class PaginationWrapper {
    private int askedPage;
    private int elementsPerPage;

    @SerializedName("usersFilters")
    private UsersPanelFilters usersFilters;
    private String orderBy;

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

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    @Override
    public String toString() {
        Filters current = null;

        if(usersFilters != null)
            current = this.getUsersFilters();

        return String.format("{\n" +
                "   AskedPage: %d,\n" +
                "   ElementsPerPage: %d,\n" +
                "   Filters: %s,\n" +
                "   OrderBy: %s,\n" +
                "}", this.getAskedPage(), this.getElementsPerPage(), current, this.getOrderBy());
    }
}
