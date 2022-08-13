package com.github.DiachenkoMD.web.daos.prototypes;

import com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.LimitedInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.PanelInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.RepairInvoice;
import com.github.DiachenkoMD.entities.exceptions.DBException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface InvoicesDAO {
    HashMap<LimitedInvoice, String> getBasicConnectedWithCar(int carId) throws DBException;

    /**
     * Method to get invoice coupled with specific car
     * @param carId id of the car with which invoices coupled
     * @return {@link HashMap} of invoice id to client email.
     * @throws DBException
     */
    HashMap<Integer, String> getOnCar(int carId) throws DBException;

    List<PanelInvoice> getPanelInvoicesWithFilters(HashMap<String, String> filters, List<String> orderBy, int limitOffset, int limitCount) throws DBException;
    int getPanelInvoicesNumberWithFilters(HashMap<String, String> filters) throws DBException;

    InformativeInvoice getInvoiceDetails(int invoiceId) throws DBException;

    void createRepairInvoice(int invoiceId, BigDecimal price, LocalDate expirationDate, String comment) throws DBException;

    void deleteRepairInvoice(int repairId) throws DBException;

    Optional<RepairInvoice> getRepairInvoiceInfo(int repairId) throws DBException;

    void rejectInvoice(int invoiceId, String reason) throws DBException;

    /**
     * Method for getting calculated stats. As the result of the execution, you will get: <br/>
     * <ul>
     *     <li>Rents in progress - rents, which date_start are in this month, but before tomorrows date.</li>
     *     <li>New invoices - invoices, which have been created from 1 day of current month.</li>
     *     <li>Earnings this month - sum of all invoices + 35% of sum of all repair invoices (35% goes to company and other for real repairment). Counts for this month.</li>
     * </ul>
     * @return
     * @throws DBException
     */
    List<Double> getStats() throws DBException;
}
