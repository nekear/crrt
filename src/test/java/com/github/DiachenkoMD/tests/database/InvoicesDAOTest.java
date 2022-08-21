package com.github.DiachenkoMD.tests.database;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.invoices.ClientInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.DriverInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.RepairInvoice;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.github.DiachenkoMD.entities.enums.InvoiceStatuses;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.extensions.ConnectionParameterResolverExtension;
import com.github.DiachenkoMD.extensions.DatabaseOperationsExtension;
import com.github.DiachenkoMD.utils.TGenerators;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlCarsDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlInvoicesDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlUsersDAO;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({
        DatabaseOperationsExtension.class,
        ConnectionParameterResolverExtension.class
})
class InvoicesDAOTest {

    private final UsersDAO usersDAO;
    private final InvoicesDAO invoicesDAO;
    private final CarsDAO carsDAO;

    public InvoicesDAOTest(DataSource ds){
        this.usersDAO = new MysqlUsersDAO(ds);
        this.invoicesDAO = new MysqlInvoicesDAO(ds);
        this.carsDAO = new MysqlCarsDAO(ds);
    }

    private Car car;
    private int carId;
    private LimitedUser client;
    private int clientId;
    private Passport passport;
    private DatesRange datesRange;

    @BeforeEach
    public void setup() throws DBException {
        // Generating new invoice
        car = TGenerators.genCar();
        carId = carsDAO.create(car);
        car.setId(carId);

        client = TGenerators.genUser();
        clientId = usersDAO.insertUser(client);
        client.setId(clientId);

        passport = TGenerators.genPassport();
        datesRange = TGenerators.genDatesRange();

    }

    @Test
    void getInvoicesToClientsOnCar() throws DBException {
        int invoiceId = invoicesDAO.createInvoice(carId, clientId, datesRange, passport, BigDecimal.valueOf(1000), null);

        // Doing checks
        HashMap<Integer, String> invoicesToClients = invoicesDAO.getInvoicesToClientsOnCar(carId);

        assertEquals(1, invoicesToClients.size());
        assertTrue(invoicesToClients.containsKey(invoiceId));
        assertEquals(client.getEmail(), invoicesToClients.get(invoiceId));
    }

    @Test
    void getInvoiceDetails() throws DBException {
        int invoiceId = invoicesDAO.createInvoice(carId, clientId, datesRange, passport, BigDecimal.valueOf(1000), null);

        // Adding repairment invoice
        invoicesDAO.createRepairInvoice(invoiceId, BigDecimal.valueOf(1000), LocalDate.of(2022, 10, 10), null);

        // Getting invoice details and doing checks
        InformativeInvoice invoice = invoicesDAO.getInvoiceDetails(invoiceId);

        assertThat(invoice.getPassport()).usingRecursiveComparison().isEqualTo(passport);
        assertThat(invoice.getClientEmail()).isEqualTo(client.getEmail());
        assertThat(invoice.getCity()).isEqualTo(car.getCity());
        assertThat(invoice.getRejectionReason()).isNull();
        assertThat(invoice.getRepairInvoices().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("getRepairInvoiceInfo / createRepairInvoice")
    void getCreateRepairInvoice() throws DBException {
        int invoiceId = invoicesDAO.createInvoice(carId, clientId, datesRange, passport, BigDecimal.valueOf(1000), null);

        // Adding repairment invoice
        int repairmentInvoiceId = invoicesDAO.createRepairInvoice(invoiceId, BigDecimal.valueOf(1000), LocalDate.of(2022, 10, 10), null);

        RepairInvoice repairInvoice = invoicesDAO.getRepairInvoiceInfo(repairmentInvoiceId).get();

        // Data matching checks
        assertThat(repairInvoice.getId()).isEqualTo(repairmentInvoiceId);
        assertThat(repairInvoice.getOriginInvoiceId()).isEqualTo(invoiceId);
        assertThat(repairInvoice.getExpirationDate()).isEqualTo(LocalDate.of(2022, 10, 10));
        assertThat(repairInvoice.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void deleteRepairInvoice() throws DBException {
        int invoiceId = invoicesDAO.createInvoice(carId, clientId, datesRange, passport, BigDecimal.valueOf(1000), null);

        // Adding repairment invoice
        int repairmentInvoiceId = invoicesDAO.createRepairInvoice(invoiceId, BigDecimal.valueOf(1000), LocalDate.of(2022, 10, 10), null);

        // Deleting repairment invoice
        assertTrue(invoicesDAO.deleteRepairInvoice(repairmentInvoiceId));
        assertTrue(invoicesDAO.getRepairInvoiceInfo(repairmentInvoiceId).isEmpty());
    }


    @Test
    void rejectInvoice() throws DBException {
        String rejectionReason = "Some rejection reason";

        int invoiceId = invoicesDAO.createInvoice(carId, clientId, datesRange, passport, BigDecimal.valueOf(1000), null);

        assertTrue(invoicesDAO.rejectInvoice(invoiceId, rejectionReason));

        InformativeInvoice invoice = invoicesDAO.getInvoiceDetails(invoiceId);

        assertThat(invoice.getStatusList()).contains(InvoiceStatuses.REJECTED);
        assertThat(invoice.getRejectionReason()).isEqualTo(rejectionReason);
    }

    @Test
    void getStats() throws DBException {

        // Custom dates range to check for "new invoices"
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(10);

        invoicesDAO.createInvoice(carId, clientId, new DatesRange(start, end), passport, BigDecimal.valueOf(1000), null);

        List<Double> stats = invoicesDAO.getStats();

        assertEquals(3, stats.size());
        assertEquals(1d, stats.get(0)); // rentsInProgress
        assertEquals(1d, stats.get(1)); // newInvoices
        assertEquals(1000d, stats.get(2)); // earningsThisMonth
    }

    @Test
    void getInvoicesForClient() throws DBException {
        int invoiceId = invoicesDAO.createInvoice(carId, clientId, datesRange, passport, BigDecimal.valueOf(1000), null);

        List<ClientInvoice> clientInvoices = invoicesDAO.getInvoicesForClient(clientId);

        assertEquals(1, clientInvoices.size());
        assertEquals(invoiceId, clientInvoices.get(0).getId());
    }

    @Test
    void getInvoicesForDriver() throws DBException {
        int driverId = usersDAO.insertDriver(clientId, car.getCity().id());

        int invoiceId = invoicesDAO.createInvoice(carId, clientId, datesRange, passport, BigDecimal.valueOf(1000), driverId);

        // Executing method under test
        List<DriverInvoice> driverInvoices = invoicesDAO.getInvoicesForDriver(clientId);


        // Checks
        assertEquals(1, driverInvoices.size());

        DriverInvoice invoice =  driverInvoices.get(0);
        assertEquals(invoiceId, invoice.getId());
        assertEquals(car.getModel(), invoice.getModel());
        assertEquals(car.getBrand(), invoice.getBrand());
        assertEquals(car.getCity(), invoice.getCity());
        assertThat(invoice.getSalary()).isPositive();
    }

    @Test
    void payRepairInvoice() throws DBException {
        int invoiceId = invoicesDAO.createInvoice(carId, clientId, datesRange, passport, BigDecimal.valueOf(1000), null);

        int repairInvoiceId = invoicesDAO.createRepairInvoice(invoiceId, BigDecimal.valueOf(100), LocalDate.now().plusDays(20), null);

        // Calling actual method under test and doing checks
        assertTrue(invoicesDAO.payRepairInvoice(repairInvoiceId));

        RepairInvoice repairInvoice = invoicesDAO.getRepairInvoiceInfo(repairInvoiceId).get();

        assertTrue(repairInvoice.isPaid());
    }

    @Test
    void cancelInvoice() throws DBException {
        int invoiceId = invoicesDAO.createInvoice(carId, clientId, datesRange, passport, BigDecimal.valueOf(1000), null);

        // Calling actual method under test and doing checks
        assertTrue(invoicesDAO.cancelInvoice(invoiceId));

        InformativeInvoice invoice = invoicesDAO.getInvoiceDetails(invoiceId);

        assertThat(invoice.getStatusList()).contains(InvoiceStatuses.CANCELED);
    }

    @Test
    void createInvoice() throws DBException {
        int invoiceId = invoicesDAO.createInvoice(carId, clientId, datesRange, passport, BigDecimal.valueOf(1000), null);

        assertThat(invoiceId).isPositive();
    }

    @Test
    void setInvoiceDriver() throws DBException {
        int invoiceId = invoicesDAO.createInvoice(carId, clientId, datesRange, passport, BigDecimal.valueOf(1000), null);

        int driverId = usersDAO.insertDriver(clientId, car.getCity().id());

        assertTrue(invoicesDAO.setInvoiceDriver(invoiceId, driverId));

        InformativeInvoice invoice = invoicesDAO.getInvoiceDetails(invoiceId);

        assertThat(invoice.getDriver().getEmail()).isEqualTo(client.getEmail());
    }
}