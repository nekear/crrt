package com.github.DiachenkoMD.web.daos.prototypes;

import com.github.DiachenkoMD.entities.dto.invoices.Invoice;
import com.github.DiachenkoMD.entities.exceptions.DBException;

import java.util.HashMap;

public interface InvoicesDAO {
    HashMap<Invoice, String> getBasicConnectedWithCar(int carId) throws DBException;

    HashMap<Integer, String> getOnCar(int carId) throws DBException;

    
}
