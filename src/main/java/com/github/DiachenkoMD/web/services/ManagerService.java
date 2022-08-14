package com.github.DiachenkoMD.web.services;

import com.github.DiachenkoMD.entities.adapters.CryptoAdapter;
import com.github.DiachenkoMD.entities.dto.*;
import com.github.DiachenkoMD.entities.dto.drivers.LimitedDriver;
import com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.PanelInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.RepairInvoice;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.enums.InvoiceStatuses;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.google.gson.Gson;
import com.google.gson.annotations.JsonAdapter;
import jakarta.servlet.ServletContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.github.DiachenkoMD.web.utils.Utils.*;

public class ManagerService {

    private static final Logger logger = LogManager.getLogger(ManagerService.class);
    private final UsersDAO usersDAO;
    private final InvoicesDAO invoicesDAO;

    private final ServletContext ctx;

    public ManagerService(UsersDAO usersDAO, InvoicesDAO invoicesDAO, ServletContext ctx){
        this.usersDAO = usersDAO;
        this.invoicesDAO = invoicesDAO;
        this.ctx = ctx;
    }


    /**
     * Method for acquiring invoices list used at admin-panel and manager-panel. <br/>
     * @param paginationRequest
     * @return {@link PaginationResponse<PanelInvoice>} with {@link PanelInvoice} list inside entities field.
     */
    public PaginationResponse<PanelInvoice> getInvoices(PaginationRequest paginationRequest) throws DBException {
        logger.debug(paginationRequest);

        int askedPage = paginationRequest.getAskedPage();
        int elementsPerPage = paginationRequest.getElementsPerPage();

        int limitOffset = (askedPage - 1) * elementsPerPage;
        int limitCount = elementsPerPage;

        HashMap<String, String> searchCriteria = paginationRequest.getInvoicesFilters().getDBPresentation();

        logger.info(searchCriteria);

        List<String> orderBy = paginationRequest.getInvoicesFilters().getOrderPresentation();

        logger.info(orderBy);

        PaginationResponse<PanelInvoice> paginationResponse = new PaginationResponse<>();
        paginationResponse.setResponseData(invoicesDAO.getPanelInvoicesWithFilters(searchCriteria, orderBy, limitOffset, limitCount));
        paginationResponse.setTotalElements(invoicesDAO.getPanelInvoicesNumberWithFilters(searchCriteria));

        return paginationResponse;
    }

    /**
     * Method for acquiring invoice details, including Passport data and Repairment invoices. Designed for use in admin-panel and manager-panel, when clicking on invoice row in data-tables.
     * @param invoiceIdEncrypted encrypted invoice id.
     * @return {@link InformativeInvoice}
     * @throws DescriptiveException
     * @throws DBException
     */
    public InformativeInvoice getInvoiceDetails(String invoiceIdEncrypted) throws DescriptiveException, DBException {
        int invoiceId = Integer.parseInt(CryptoStore.decrypt(invoiceIdEncrypted));

        return invoicesDAO.getInvoiceDetails(invoiceId);
    }

    /**
     * Method for creating repairment invoices. Inside chained with {@link InvoicesDAO#getInvoiceDetails(int)} to return updated invoice data and reload it on the admin`s/manager`s page.
     * @param jsonBody should contain originId! (id of the invoice for which we create repairment invoice), price!, expirationDate! and comment?.
     * @return {@link InformativeInvoice} with updated invoice data
     * @throws DescriptiveException with {@link ExceptionReason#VALIDATION_ERROR VALIDATION_ERROR} or {@link ExceptionReason#REP_INVOICE_EXPIRATION_SHOULD_BE_LATER REP_INVOICE_EXPIRATION_SHOULD_BE_LATER}.
     * @throws DBException caused by {@link InvoicesDAO#createRepairInvoice(int, BigDecimal, LocalDate, String) createRepairInvoice} or {@link InvoicesDAO#getInvoiceDetails(int) getInvoiceDetails}
     */
    public InformativeInvoice createRepairmentInvoice(String jsonBody) throws DescriptiveException, DBException {
        Gson gson = (Gson) ctx.getAttribute("gson");

        CreateRepairmentInvoiceJPC parsedBody = gson.fromJson(jsonBody, CreateRepairmentInvoiceJPC.class);

        int invoiceId = (Integer) parsedBody.getOriginId();
        BigDecimal price = parsedBody.getPrice();
        LocalDate expirationDate = parsedBody.getExpirationDate();
        String comment = parsedBody.getComment();

        if(price == null || expirationDate == null)
            throw new DescriptiveException("Price or expiration date are null!", ExceptionReason.VALIDATION_ERROR);

        if(expirationDate.isBefore(LocalDate.now()))
            throw new DescriptiveException("Repairment invoice expiration date should be greater than previuos date", ExceptionReason.REP_INVOICE_EXPIRATION_SHOULD_BE_LATER);

        invoicesDAO.createRepairInvoice(invoiceId, price, expirationDate, comment);

        return invoicesDAO.getInvoiceDetails(invoiceId);
    }

    /**
     * Method for deleting repairment invoices. Used only at admin-panel and manager-panel. It will refund money if repairment invoice has been paid and client hasn`t been blocked. Although, notification about refund will be sent to client`s email.
     * @param originInvoiceIdEncrypted encrypted id of invoice with which this repairment invoice coupled. Needed to reload invoice data on client`s side.
     * @param repairInvoiceIdEncrypted encrypted repairment invoice id.
     * @return {@link InformativeInvoice} with updated invoice data.
     */
    public InformativeInvoice deleteRepairmentInvoice(String originInvoiceIdEncrypted, String repairInvoiceIdEncrypted) throws DescriptiveException, DBException {
        int originInvoiceId = Integer.parseInt(CryptoStore.decrypt(originInvoiceIdEncrypted));
        int repairInvoiceId = Integer.parseInt(CryptoStore.decrypt(repairInvoiceIdEncrypted));

        // Getting repairment invoice data to inform client about it`s deletion if needed (in that case money will be returned)
        RepairInvoice repairInvoice = invoicesDAO.getRepairInvoiceInfo(repairInvoiceId).orElseThrow(() -> new DescriptiveException("Failed to get repairment invoice data from db", ExceptionReason.ACQUIRING_ERROR));

        // Deleting repairment invoice entry from db
        invoicesDAO.deleteRepairInvoice(repairInvoiceId);
        logger.trace("Repairment invoice [#{}] deleted successfully from {}.", repairInvoice.getId(), repairInvoice.getClientEmail());
        // Deciding whether we should notify user or not
        if(repairInvoice.isPaid()){
            AuthUser client = usersDAO.get(repairInvoice.getClientEmail());

            // If we blocked user than there is no sense to send him notification or refund money :)
            if(!client.isBlocked()){
                usersDAO.setBalance((Integer) client.getId(),client.getBalance() + repairInvoice.getPrice().doubleValue()); // should better use BigDecimal for balance from the start but now its too late to change it
                emailNotify(client, "Refund for cancelled repairment invoice", "We apologize for the inconvenience. Apparently, this repairment invoice was billed by mistake, but since you paid for it, we gave you your money back.");
                logger.trace("Money refunded from repairment invoice [#{}] to {} at amount of {}$.", repairInvoice.getId(), repairInvoice.getClientEmail(), repairInvoice.getPrice());
            }
        }

        return invoicesDAO.getInvoiceDetails(originInvoiceId);
    }

    public InformativeInvoice rejectInvoice(String invoiceIdEncrypted, String rejectionReason) throws DescriptiveException, DBException {
        int invoiceId = Integer.parseInt(CryptoStore.decrypt(invoiceIdEncrypted));

        // Getting informative invoice to check some data
        InformativeInvoice informativeInvoice = invoicesDAO.getInvoiceDetails(invoiceId);

        // Checking whether it was already cancelled
        if(informativeInvoice.getStatusList().contains(InvoiceStatuses.CANCELED))
            throw new DescriptiveException("Unable to reject invoice, because it has been already cancelled by client!", ExceptionReason.INVOICE_ALREADY_CANCELLED);

        // Checking whether it was already rejected or not
        if(informativeInvoice.getStatusList().contains(InvoiceStatuses.REJECTED))
            throw new DescriptiveException("Unable to reject invoice, because it has been already rejected!", ExceptionReason.INVOICE_ALREADY_REJECTED);

        // if invoice is active (it is already started) or his dates range is in past, then there is no any sense to reject it
        if(informativeInvoice.getDatesRange().getEnd().isBefore(LocalDate.now()))
            throw new DescriptiveException("Unable to reject invoice because, it has already expired!", ExceptionReason.INVOICE_ALREADY_EXPIRED);

        if(informativeInvoice.getDatesRange().getStart().isBefore(LocalDate.now().plusDays(1)))
            throw new DescriptiveException("Unable to reject invoice because, it has already started!", ExceptionReason.INVOICE_ALREADY_STARTED);

        invoicesDAO.rejectInvoice(invoiceId, rejectionReason);

        // Informing user that his invoice was rejected
        String clientEmail = informativeInvoice.getClientEmail();
        AuthUser client = usersDAO.get(clientEmail);
        // If user not blocked otherwise there is no sense to notify him and make a refund
        if(!client.isBlocked()){
            usersDAO.setBalance((Integer) client.getId(),client.getBalance() + informativeInvoice.getPrice().doubleValue()); // should better use BigDecimal for balance from the start but now its too late to change it
            emailNotify(client, "Refund for rejected invoice",
                    String.format("Your car order has been rejected. Reason: %s. Car: %s. The money in the amount of %s has been returned to your balance. We apologize for the inconvenience.",
                            rejectionReason, informativeInvoice.getBrand() + informativeInvoice.getModel(), informativeInvoice.getPrice()));

            logger.trace("Money refunded from invoice [#{}] to {} at amount of {}$. Reason: {}.", informativeInvoice.getId(), client.getEmail(), informativeInvoice.getPrice(), rejectionReason);
        }

        // If invoice had connected driver, we should notify him that he was disconnected him from that invoice and returned to drivers pool
        if(informativeInvoice.getDriver() != null){
            LimitedDriver driver = informativeInvoice.getDriver();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String dateStart = informativeInvoice.getDatesRange().getStart().format(formatter);
            String dateEnd = informativeInvoice.getDatesRange().getEnd().format(formatter);

            emailNotify(informativeInvoice.getDriver().getEmail(), "Invoice unbinding", String.format("Good afternoon, driver %s. You have been disconnected from the invoice scheduled on %s to %s.", driver.getEmail(), dateStart, dateEnd));
        }

        // Updating informative invoice entity and returning to client
        informativeInvoice.getStatusList().add(InvoiceStatuses.REJECTED);
        informativeInvoice.setRejectionReason(rejectionReason);

        return informativeInvoice;
    }

    /**
     * JSON Parsing Class (JPC) for GSON. Created for parsing incoming json data for repairment invoice creation at {@link #createRepairmentInvoice(String)}. <br/>
     */
    private static class CreateRepairmentInvoiceJPC{
        @JsonAdapter(CryptoAdapter.class)
        private Object originId; // id of invoice which will be "parent" to repairment invoice

        private BigDecimal price;

        private LocalDate expirationDate;

        private String comment;

        public Object getOriginId() {
            return originId;
        }

        public void setOriginId(Object originId) {
            this.originId = originId;
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
    }
}
