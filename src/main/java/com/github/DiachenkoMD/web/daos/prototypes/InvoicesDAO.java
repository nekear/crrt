package com.github.DiachenkoMD.web.daos.prototypes;

import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.invoices.*;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.github.DiachenkoMD.entities.exceptions.DBException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface InvoicesDAO {
    /**
     * Method to get invoice coupled with specific car.
     * @param carId id of the car with which invoices coupled.
     * @return {@link HashMap} of invoice id to client email.
     * @throws DBException
     */
    HashMap<Integer, String> getInvoicesToClientsOnCar(int carId) throws DBException;

    /**
     * Method to get invoices with filters and paginated. (used in pair with {@link #getPanelInvoicesNumberWithFilters(HashMap) this} method.
     * @param filters HashMap that consists of [column name] - [searching value] pairs. To have proper form, should be acquired from {@link InvoicePanelFilters#getDBPresentation()}.
     * @param orderBy contains depending on which results will be sorted. To have proper form, should be acquired from {@link InvoicePanelFilters#getOrderPresentation()}.
     * @param limitOffset
     * @param limitCount
     * @return
     * @throws DBException
     */
    List<PanelInvoice> getPanelInvoicesWithFilters(HashMap<String, String> filters, List<String> orderBy, int limitOffset, int limitCount) throws DBException;

    /**
     * Method to get total amount of found invoices with filtering. Used in pair with {@link #getPanelInvoicesWithFilters(HashMap, List, int, int) this} method.
     * @param filters HashMap that consists of [column name] - [searching value] pairs. To have proper form, should be acquired from {@link InvoicePanelFilters#getDBPresentation()}.
     * @return amount of found invoices that satisfy filters.
     * @throws DBException
     */
    int getPanelInvoicesNumberWithFilters(HashMap<String, String> filters) throws DBException;

    /**
     * Method for getting detailed invoice info. Used at admin-panel and manager-panel.
     * @param invoiceId
     * @return
     * @throws DBException
     */
    InformativeInvoice getInvoiceDetails(int invoiceId) throws DBException;

    /**
     * Method for creating repairment invoices.
     * @param invoiceId
     * @param price
     * @param expirationDate
     * @param comment
     * @return id of newly generated repairment invoice
     * @throws DBException
     */
    int createRepairInvoice(int invoiceId, BigDecimal price, LocalDate expirationDate, String comment) throws DBException;

    /**
     * Method for deleting repairment invoices.
     * @param repairId
     * @return boolean value depending on whether entry was deleted or not.
     * @throws DBException
     */
    boolean deleteRepairInvoice(int repairId) throws DBException;

    /**
     * Method for getting detailed info about repairment invoice.
     * @param repairId
     * @return
     * @throws DBException
     */
    Optional<RepairInvoice> getRepairInvoiceInfo(int repairId) throws DBException;

    /**
     * Method for rejecting invoices. Accepts invoices id and reason (might be null, if not specified on UI).
     * @param invoiceId
     * @param reason rejection reason. Can be null, if needed.
     * @throws DBException
     */
    boolean rejectInvoice(int invoiceId, String reason) throws DBException;

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

    /**
     * Method for obtaining invoices for specific client. Used at client-panel, where clients can manage their invoices.
     * @return
     * @throws DBException
     */
    List<ClientInvoice> getInvoicesForClient(int clientId) throws DBException;

    /**
     * Despite it`s name, this method gets connected invoices to specific driver by user`s id. This was made to reduce amount of method used in process of getting such list, and, therefore number of calls to db.
     * @param userId id of user (user_id column in tbl_drivers) -> NOT DRIVER`S ID <-
     * @return list of invoices for driver panel without code and repairs statuses
     * @throws DBException
     */
    List<DriverInvoice> getInvoicesForDriver(int userId) throws DBException;

    /**
     * Method for paying for repairment invoices. Just sets status "is_paid" in db to true. Money charge should be maid in another method, if needed.
     *
     * @param repairInvoiceId
     * @return
     * @throws DBException
     */
    boolean payRepairInvoice(int repairInvoiceId) throws DBException;

    /**
     * Method for cancelling invoices. Doesn`t contain any checks for whether it is possible to cancel this invoice or not. Those checks should be maid in another method, if needed.
     * @param invoiceId
     * @throws DBException
     */
    boolean cancelInvoice(int invoiceId) throws DBException;

    /**
     * Method for creating new invoice. Used when user creates new rent invoice. <br/>
     * Talking about process under the hood, here are the steps in general:
     * <ol>
     *     <li>Generating unique invoice code (consists of 7 characters), uses {@link com.github.DiachenkoMD.web.utils.Utils#generateRandomString(int) this} method.</li>
     *     <li>New row is created in the db with entered passport data. Better approach, imho, is to allow user select previously entered passport presets, but developing this feature may take a lot of time, which I don`t have.</li>
     *     <li>New invoice is being created in the db. Fields like is_cancelled and is_rejected are ommited. Driver is set "as is" and if incoming driver is null, then it will be null, if not, then it will equals to some driver`s id.</li>
     *     <li>Getting current client`s balance.</li>
     *     <li>Calculating new balance and updating client`s entry with new balance.</li>
     *     <li>Returning newly generated invoice id. (from step 3)</li>
     * </ol>
     * <pre>Note: there is no check whether client have enough money or not, but money are withdrawn inside</pre>
     * @param carId id of the car, invoice will be linked to
     * @param clientId id of the client, renting the car
     * @param range range of dates, rent is applied on
     * @param passport passport data of the client
     * @param exp_price price of invoice. Expected, that it will be counted like [car_price (per day)] * amount of days.
     * @param driverId is parameter is not "int" like carId or clientId, that`s because it may be null, meaning that there is no driver specified
     * @return id of newly created invoice
     * @throws DBException
     */
    int createInvoice(int carId, int clientId, DatesRange range, Passport passport, BigDecimal exp_price, Integer driverId) throws DBException;

    /**
     * Method for coupling (or decoupling) driver with specified invoice.
     * @param invoiceId
     * @param driverId represented as Integer ot allow passing <i>"null"</i> value, if needed to decouple driver.
     * @return boolean value depending on whether entity was found and updated or not.
     * @throws DBException
     */
    boolean setInvoiceDriver(int invoiceId, Integer driverId) throws DBException;

    /**
     * Method for getting invoices for generating reports.
     * @return list of {@link InformativeInvoice}.
     * @throws DBException
     */
    List<InformativeInvoice> getInvoicesForReport() throws DBException;
}
