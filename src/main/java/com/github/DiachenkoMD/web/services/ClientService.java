package com.github.DiachenkoMD.web.services;

import com.github.DiachenkoMD.entities.adapters.CryptoAdapter;
import com.github.DiachenkoMD.entities.dto.PaginationRequest;
import com.github.DiachenkoMD.entities.dto.PaginationResponse;
import com.github.DiachenkoMD.entities.dto.drivers.LimitedDriver;
import com.github.DiachenkoMD.entities.dto.invoices.ClientInvoice;
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
import java.util.HashMap;
import java.util.List;

import static com.github.DiachenkoMD.web.utils.Utils.emailNotify;

public class ClientService {

    private static final Logger logger = LogManager.getLogger(ClientService.class);
    private final UsersDAO usersDAO;
    private final InvoicesDAO invoicesDAO;

    private final ServletContext ctx;

    public ClientService(UsersDAO usersDAO, InvoicesDAO invoicesDAO, ServletContext ctx) {
        this.usersDAO = usersDAO;
        this.invoicesDAO = invoicesDAO;
        this.ctx = ctx;
    }


    /**
     * Method for acquiring invoices connected with client. Simply calls {@link InvoicesDAO#getInvoicesForClient(int)}.
     * @param clientId
     * @return list of invoices packed into {@link ClientInvoice} objects to limit transported info.
     * @throws DBException from {@link InvoicesDAO#getInvoicesForClient(int)}.
     */
    public List<ClientInvoice> getInvoices(int clientId) throws DBException {
        return invoicesDAO.getInvoicesForClient(clientId);
    }

    /** Method for getting invoice details for client. All data is obtained from {@link InvoicesDAO#getInvoiceDetails(int)}, but driver and clientEmail are set to null
     * to not expose this info to client (end-user).
     * @param invoiceId
     * @return {@link InformativeInvoice} with empty <strong>driver</strong> and <strong>clientEmail</strong> fields.
     * @throws DBException may be thrown from {@link InvoicesDAO#getInvoiceDetails(int)}
     */
    public InformativeInvoice getInvoiceDetails(int invoiceId) throws DBException {
        InformativeInvoice informativeInvoice = invoicesDAO.getInvoiceDetails(invoiceId);


        if(informativeInvoice.getDriver() != null){
            List<InvoiceStatuses> statuses = informativeInvoice.getStatusList();
            statuses.add(InvoiceStatuses.WITH_DRIVER);
            informativeInvoice.setStatusList(statuses);
        }

        // Clearing data which should not be exposed to end-user.
        informativeInvoice.setDriver(null);
        informativeInvoice.setClientEmail(null);

        return informativeInvoice;
    }

    /**
     * Method for paying repairment invoices. Contains check for balance (user balance is obtained from db) and for repairment invoice aliveness (or will throw {@link DescriptiveException}).
     * @param repairInvoiceId
     * @param client
     * @return {@link InformativeInvoice} to update necessary data on client side.
     * @throws DBException might be thrown by {@link UsersDAO#get(int)}, {@link InvoicesDAO#getRepairInvoiceInfo(int)}.
     * @throws DescriptiveException with reasons {@link ExceptionReason#REP_INVOICE_WAS_NOT_FOUND REP_INVOICE_WAS_NOT_FOUND}, {@link ExceptionReason#NOT_ENOUGH_MONEY NOT_ENOUGH_MONEY}.
     */
    public InformativeInvoice payRepairmentInvoice(int repairInvoiceId, AuthUser client) throws DBException, DescriptiveException {

        // Getting repairment data
        RepairInvoice repairInvoice = invoicesDAO.getRepairInvoiceInfo(repairInvoiceId).orElseThrow(() -> new DescriptiveException("No repair invoice found with this id", ExceptionReason.REP_INVOICE_WAS_NOT_FOUND));

        // Getting client id
        int clientId = client.getCleanId().get();

        // Converting balance to BigDecimal. Using BigDecimal for balance is much better practise imho, but its too late to make full switch.
        BigDecimal clientBalance = BigDecimal.valueOf(usersDAO.getBalance(clientId));

        // Checking if client has enough money to pay for invoice
        if(clientBalance.compareTo(repairInvoice.getPrice()) < 0)
            throw new DescriptiveException("Client`s balance is " + clientBalance + " but repairment invoice price is " + repairInvoice.getPrice(), ExceptionReason.NOT_ENOUGH_MONEY);

        // This invoice might be already paid, so its better to check
        if(repairInvoice.isPaid())
            throw new DescriptiveException(String.format("Repairment invoice %s is already paid", repairInvoice), ExceptionReason.REP_INVOICE_IS_ALREADY_PAID);

        invoicesDAO.payRepairInvoice(repairInvoiceId);

        BigDecimal newBalance = clientBalance.subtract(repairInvoice.getPrice());

        // Updating balance in db
        usersDAO.setBalance(clientId, newBalance);

        // Updating balance in session "auth" entity
        client.setBalance(newBalance.doubleValue());

        // Returning updated invoice data
        return this.getInvoiceDetails(repairInvoice.getOriginInvoiceId());
    }

    /**
     * Method for canceling invoice. For 2022.08.14, expected, that only clients will be able to use it. At the end, if all conditions passed, money is refunded and if invoice had connected driver, that driver will be informed. <br/>
     * Inside contains checks for was invoice already rejected, was it already cancelled, did he expired or just stared.
     * @param invoiceId
     * @param client
     * @throws DescriptiveException may be thrown with reasons INVOICE_ALREADY_REJECTED, INVOICE_ALREADY_CANCELLED, INVOICE_ALREADY_EXPIRED, INVOICE_ALREADY_STARTED.
     * @throws DBException may come from {@link InvoicesDAO#getInvoiceDetails(int)}, {@link UsersDAO#getBalance(int)}, {@link InvoicesDAO#cancelInvoice(int)}, {@link UsersDAO#setBalance(int, double)}.
     */
    public void cancelInvoice(int invoiceId, AuthUser client) throws DescriptiveException, DBException {
        // Getting client id
        int clientId = (Integer) client.getId();

        // Getting invoice data (mainly to check price and whether it already rejected / cancelled or not)
        InformativeInvoice informativeInvoice = invoicesDAO.getInvoiceDetails(invoiceId);

        // If target invoice was already rejected, we cant allow user to cancel it, because we have already made him a refund
        if(informativeInvoice.getStatusList().contains(InvoiceStatuses.REJECTED))
            throw new DescriptiveException("Unable to cancel target invoice, because it is already rejected", ExceptionReason.INVOICE_ALREADY_REJECTED);

        // If invoice already cancelled
        if(informativeInvoice.getStatusList().contains(InvoiceStatuses.CANCELED))
            throw new DescriptiveException("Unable to cancel target invoice, because it is already cancelled", ExceptionReason.INVOICE_ALREADY_CANCELLED);

        // If invoice expired
        if(informativeInvoice.getDatesRange().getEnd().isBefore(LocalDate.now()))
            throw new DescriptiveException("Unable to cancel target invoice, because it is already expired", ExceptionReason.INVOICE_ALREADY_EXPIRED);

        // If invoice is active (already started)
        if(informativeInvoice.getDatesRange().getStart().isBefore(LocalDate.now().plusDays(1)))
            throw new DescriptiveException("Unable to cancel target invoice, because it is already started", ExceptionReason.INVOICE_ALREADY_STARTED);

        BigDecimal clientBalance = BigDecimal.valueOf(usersDAO.getBalance(clientId));

        // Cancelling invoice
        invoicesDAO.cancelInvoice(invoiceId);

        // Refunding client his money for the invoice
        BigDecimal newBalance = informativeInvoice.getPrice().add(clientBalance);
        usersDAO.setBalance(clientId, newBalance);
        client.setBalance(newBalance.doubleValue());

        // If invoice had coupled driver, then informing driver that he "lost" his connected invoice
        if(informativeInvoice.getDriver() != null){
            String driverEmail = informativeInvoice.getDriver().getEmail();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String dateStart = informativeInvoice.getDatesRange().getStart().format(formatter);
            String dateEnd = informativeInvoice.getDatesRange().getEnd().format(formatter);

            emailNotify(driverEmail, "Invoice was uncoupled",
                    String.format("A customer who had rented a <strong>%s</strong> from <strong>%s</strong> to <strong>%s</strong> decided to cancel it. The reservation has been released from your list. Have a nice day.",
                            informativeInvoice.getBrand() + " " + informativeInvoice.getModel(),
                                dateStart, dateEnd)
                    );
        }
    }
}