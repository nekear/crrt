package com.github.DiachenkoMD.web.services;

import com.github.DiachenkoMD.entities.dto.drivers.ExtendedDriver;
import com.github.DiachenkoMD.entities.dto.invoices.ClientInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.DriverInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.RepairInvoice;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.enums.Cities;
import com.github.DiachenkoMD.entities.enums.InvoiceStatuses;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import jakarta.servlet.ServletContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import static com.github.DiachenkoMD.web.utils.Utils.emailNotify;

public class DriverService {

    private static final Logger logger = LogManager.getLogger(DriverService.class);
    private static final Marker DB_MARKER = MarkerManager.getMarker("DB");
    private final UsersDAO usersDAO;
    private final InvoicesDAO invoicesDAO;

    private final ServletContext ctx;

    public DriverService(UsersDAO usersDAO, InvoicesDAO invoicesDAO, ServletContext ctx) {
        this.usersDAO = usersDAO;
        this.invoicesDAO = invoicesDAO;
        this.ctx = ctx;
    }


    /**
     * Method for acquiring invoices connected with driver. Inside just calls {@link InvoicesDAO#getInvoicesForDriver(int)}.
     * @param userId
     * @return list of invoices packed into {@link DriverInvoice} objects to limit transported info.
     * @throws DBException from {@link InvoicesDAO#getInvoicesForDriver(int)}.
     */
    public List<DriverInvoice> getInvoices(int userId) throws DBException {
        return invoicesDAO.getInvoicesForDriver(userId);
    }

    public boolean skipInvoice(int invoiceId, AuthUser user) throws DBException, DescriptiveException {
        // Getting driver by user id
        ExtendedDriver driver = usersDAO.getDriverFromUser((Integer) user.getId()).orElseThrow(() -> new DescriptiveException("Unable to get driver from db by current user id", ExceptionReason.ACQUIRING_ERROR));

        // Getting invoice and checking whether current user is actual invoice driver
        InformativeInvoice invoice = invoicesDAO.getInvoiceDetails(invoiceId);
        LocalDate rangeStart = invoice.getDatesRange().getStart();
        LocalDate rangeEnd = invoice.getDatesRange().getEnd();

        if(invoice.getDriver() == null || !invoice.getDriver().getEmail().equalsIgnoreCase(driver.getEmail()))
            throw new DescriptiveException("Current user don`t have enough rights to skip this invoice", ExceptionReason.ACCESS_DENIED);

        // We cannot skip invoice if it has already started or was cancelled or rejected
        List<InvoiceStatuses> statusesList = invoice.getStatusList();
        if(rangeStart.isBefore(LocalDate.now().plusDays(1)) || statusesList.contains(InvoiceStatuses.CANCELED) || statusesList.contains(InvoiceStatuses.REJECTED))
            throw new DescriptiveException("This invoice might has already started, has been cancelled or rejected", ExceptionReason.ACCESS_DENIED);

        // Getting available drivers and looking for another suitable employee
        List<Integer> availableDrivers = usersDAO.getAvailableDriversOnRange(rangeStart, rangeEnd, invoice.getCity().id());

        if(availableDrivers.size() == 0)
            return false;

        // Getting new driver id
        Integer newDriverId = availableDrivers.get(new Random().nextInt(availableDrivers.size()));

        // Setting new driver to invoice
        invoicesDAO.setInvoiceDriver(invoiceId, newDriverId);

        // Getting new driver data to notify him
        LimitedUser newDriver = usersDAO.getFromDriver(newDriverId).orElseThrow(() -> new DescriptiveException(String.format("Unable to obtain user information by driver id [%s] ", newDriverId), ExceptionReason.ACQUIRING_ERROR));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String rentStartFormatted = rangeStart.format(formatter);
        String rentEndFormatted = rangeEnd.format(formatter);

        emailNotify(newDriver.getEmail(), "New rent was added to your list", String.format("Hello, driver. New rent, scheduled from <strong>%s</strong> to <strong>%s</strong> on <strong>%s</strong> has been added to your list!", rentStartFormatted, rentEndFormatted, invoice.getBrand() + invoice.getModel()));

        logger.info(DB_MARKER, "Driver [{}] successfully skipped rent [{}]. In was delegated to driver [{}].",
                driver.getId(),
                invoiceId,
                newDriver.getId()
        );

        return true;
    }

    /**
     * Method for changing driver`s dislocation.
     * @param userId
     * @param city
     * @throws DBException may be thrown from {@link UsersDAO#getDriverFromUser(int)}
     * @throws DescriptiveException may be thrown with reasons {@link ExceptionReason#ACQUIRING_ERROR ACQUIRING_ERROR}
     */
    public void changeCity(int userId, Cities city) throws DBException, DescriptiveException {
        // Getting driver to check whether it has the same city or not
        ExtendedDriver driver = usersDAO.getDriverFromUser(userId).orElseThrow(() -> new DescriptiveException("Unable to obtain driver entity associated with incoming user id ["+userId+"]", ExceptionReason.ACQUIRING_ERROR));

        // Changing city
        if(driver.getCity().id() != city.id())
            if(!usersDAO.setDriverCity((Integer) driver.getId(), city.id()))
                throw new DescriptiveException("Zero rows were updated in db", ExceptionReason.DB_ACTION_ERROR);
    }

    public Cities getCity(int userId) throws DBException, DescriptiveException {
        ExtendedDriver driver = usersDAO.getDriverFromUser(userId).orElseThrow(() -> new DescriptiveException("Unable to obtain driver entity associated with incoming user id ["+userId+"]", ExceptionReason.ACQUIRING_ERROR));

        return driver.getCity();
    }
}