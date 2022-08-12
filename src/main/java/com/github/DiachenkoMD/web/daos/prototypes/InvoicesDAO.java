package com.github.DiachenkoMD.web.daos.prototypes;

import com.github.DiachenkoMD.entities.dto.invoices.LimitedInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.PanelInvoice;
import com.github.DiachenkoMD.entities.exceptions.DBException;

import java.util.HashMap;
import java.util.List;

public interface InvoicesDAO {
    HashMap<LimitedInvoice, String> getBasicConnectedWithCar(int carId) throws DBException;

    HashMap<Integer, String> getOnCar(int carId) throws DBException;

    List<PanelInvoice> getPanelInvoicesWithFilters(HashMap<String, String> filters, int limitOffset, int limitCount) throws DBException;
    int getPanelInvoicesNumberWithFilters(HashMap<String, String> filters) throws DBException;
}
