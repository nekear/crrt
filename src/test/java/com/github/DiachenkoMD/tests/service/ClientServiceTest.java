package com.github.DiachenkoMD.tests.service;

import com.github.DiachenkoMD.entities.adapters.DBCoupledAdapter;
import com.github.DiachenkoMD.entities.adapters.LocalDateAdapter;
import com.github.DiachenkoMD.entities.adapters.LocalDateTimeAdapter;
import com.github.DiachenkoMD.entities.adapters.Skip;
import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.invoices.ClientInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.RepairInvoice;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.github.DiachenkoMD.entities.enums.DBCoupled;
import com.github.DiachenkoMD.entities.enums.InvoiceStatuses;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.extensions.ConnectionParameterResolverExtension;
import com.github.DiachenkoMD.extensions.DatabaseOperationsExtension;
import com.github.DiachenkoMD.utils.TGenerators;
import com.github.DiachenkoMD.utils.TStore;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlCarsDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlInvoicesDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlUsersDAO;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.web.services.ClientService;
import com.github.DiachenkoMD.web.utils.RightsManager;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({
        ConnectionParameterResolverExtension.class,
        DatabaseOperationsExtension.class,
        MockitoExtension.class,
})
class ClientServiceTest {

    private final ClientService clientService;
    private final UsersDAO usersDAO; // with spy
    private final InvoicesDAO invoicesDAO; // with spy
    private final CarsDAO carsDAO; // with spy

    public ClientServiceTest(DataSource ds) {
        this.usersDAO = spy(new MysqlUsersDAO(ds));
        this.invoicesDAO = spy(new MysqlInvoicesDAO(ds));
        this.carsDAO = spy(new MysqlCarsDAO(ds));

        ServletContext _ctx = mock(ServletContext.class);

        Gson gson = TStore.getGson();

        lenient().when(_ctx.getAttribute("gson")).thenReturn(gson);
        lenient().when(_ctx.getAttribute("rights_manager")).thenReturn(mock(RightsManager.class));

        this.clientService = new ClientService(usersDAO, invoicesDAO, _ctx);
    }


    private Car car;
    private LimitedUser client;
    private Passport passport;
    private int invoiceId;

    @BeforeEach
    public void beforeEachSetup() throws DBException {
        this.client = TGenerators.genUser();
        int clientId = usersDAO.insertUser(client);
        client.setId(clientId);


        this.passport = TGenerators.genPassport();

        DatesRange datesRange = TGenerators.genDatesRange();

        this.car = TGenerators.genCar();
        int carId = carsDAO.create(car);
        this.car.setId(carId);

        // Note: inside createInvoice() method, money are withdrawn from client`s balance. Should be taken into consideration while reviewing tests.
        this.invoiceId = invoicesDAO.createInvoice(carId, clientId, datesRange, passport, BigDecimal.valueOf(1000), null);
    }

    @Test
    void getInvoices() throws DBException {
        List<ClientInvoice> foundInvoices = clientService.getInvoices((Integer) client.getId());

        assertEquals(1, foundInvoices.size());
        assertEquals(invoiceId, foundInvoices.get(0).getId());
    }

    @Test
    void getInvoiceDetails() throws DBException {
        // Adding driver to invoice
        LimitedUser driverUser = TGenerators.genUser();
        int driverUserId = usersDAO.insertUser(driverUser);
        driverUser.setId(driverUserId);

        int driverId = usersDAO.insertDriver(driverUserId, car.getCity().id());

        invoicesDAO.setInvoiceDriver(invoiceId, driverId);

        // Getting invoice id
        InformativeInvoice invoice = clientService.getInvoiceDetails(invoiceId);

        assertThat(invoice).isNotNull();
        assertThat(invoice.getStatusList()).contains(InvoiceStatuses.WITH_DRIVER);
        assertThat(invoice.getDriver()).isNull(); // should not be exposed to client
        assertThat(invoice.getClientEmail()).isNull(); // should not be exposed to client
    }

    @Nested
    class payRepairmentInvoice {
        @Test
        @DisplayName("Not enough money [fail]")
        void notEnoughMoneyFail() throws DBException {
            // Adding repairment invoice
            int repairInvoiceId = invoicesDAO.createRepairInvoice(invoiceId, BigDecimal.valueOf(1000), LocalDate.now().plusDays(4), null);
            AuthUser authClient = usersDAO.get((Integer) client.getId());

            // Note: current balance is -1000 or something like that (because invoice creation in BeforeEach)
            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> clientService.payRepairmentInvoice(repairInvoiceId, authClient));

            assertThat(expectedException.getReason()).isEqualTo(ExceptionReason.NOT_ENOUGH_MONEY);
        }

        @Test
        @DisplayName("Invoice already paid [fail]")
        void invoiceAlreadyPaidFail() throws DBException {
            // Adding repairment invoice
            int repairInvoiceId = invoicesDAO.createRepairInvoice(invoiceId, BigDecimal.valueOf(1000), LocalDate.now().plusDays(4), null);
            AuthUser authClient = usersDAO.get((Integer) client.getId());

            // Note: current balance is -1000 or something like that (because invoice creation in BeforeEach)
            usersDAO.setBalance((Integer) authClient.getId(), 2000);

            // Paying invoice to invoke "Already paid" exception
            invoicesDAO.payRepairInvoice(repairInvoiceId);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> clientService.payRepairmentInvoice(repairInvoiceId, authClient));

            assertThat(expectedException.getReason()).isEqualTo(ExceptionReason.REP_INVOICE_IS_ALREADY_PAID);
        }

        @Test
        @DisplayName("Payment successful")
        void invoicePaymentSuccessful() throws DBException, DescriptiveException {
            BigDecimal futureUserBalance = BigDecimal.valueOf(2000);
            BigDecimal repairmentInvoicePrice = BigDecimal.valueOf(1000);

            // Adding repairment invoice
            int repairInvoiceId = invoicesDAO.createRepairInvoice(invoiceId, repairmentInvoicePrice, LocalDate.now().plusDays(4), null);
            AuthUser authClient = usersDAO.get((Integer) client.getId());

            // Note: current balance is -1000 or something like that (because invoice creation in BeforeEach)
            usersDAO.setBalance((Integer) authClient.getId(), futureUserBalance);

            InformativeInvoice invoice = clientService.payRepairmentInvoice(repairInvoiceId, authClient);

            // Checking for repairment invoice being paid
            RepairInvoice repairInvoice = invoice.getRepairInvoices()
                    .stream()
                    .filter(x -> (Integer) x.getId() == repairInvoiceId)
                    .findFirst().orElse(null);

            assertThat(repairInvoice).isNotNull();
            assertThat(repairInvoice.isPaid()).isTrue();

            // Checking that client balance has changed
            BigDecimal currentUserBalance = BigDecimal.valueOf(usersDAO.getBalance((Integer) authClient.getId()));
            assertThat(currentUserBalance).isEqualByComparingTo(futureUserBalance.subtract(repairmentInvoicePrice));
        }
    }

    @Nested
    class cancelInvoice {
        private AuthUser authClient;

        @BeforeEach
        void init() throws DBException {
            authClient = usersDAO.get((Integer) client.getId());
        }

        @Test
        @DisplayName("Invoice already rejected [fail]")
        void invoiceRejectedFail() throws DBException {
            invoicesDAO.rejectInvoice(invoiceId, null);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> clientService.cancelInvoice(invoiceId, authClient));
            assertEquals(ExceptionReason.INVOICE_ALREADY_REJECTED, expectedException.getReason());
        }

        @Test
        @DisplayName("Invoice already cancelled [fail]")
        void invoiceCancelledFail() throws DBException {
            invoicesDAO.cancelInvoice(invoiceId);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> clientService.cancelInvoice(invoiceId, authClient));
            assertEquals(ExceptionReason.INVOICE_ALREADY_CANCELLED, expectedException.getReason());
        }

        @Test
        @DisplayName("Invoice already expired [fail]")
        void invoiceExpiredFail() throws DBException {
            LocalDate start = LocalDate.now().minusDays(10);
            LocalDate end = LocalDate.now().minusDays(1);

            int invoice2Id = invoicesDAO.createInvoice((Integer) car.getId(), (Integer) client.getId(), new DatesRange(start, end), passport, BigDecimal.valueOf(1000), null);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> clientService.cancelInvoice(invoice2Id, authClient));
            assertEquals(ExceptionReason.INVOICE_ALREADY_EXPIRED, expectedException.getReason());
        }

        @Test
        @DisplayName("Invoice already started [fail]")
        void invoiceStartedFail() throws DBException {
            LocalDate start = LocalDate.now().minusDays(1);
            LocalDate end = LocalDate.now().plusDays(4);

            int invoice2Id = invoicesDAO.createInvoice((Integer) car.getId(), (Integer) client.getId(), new DatesRange(start, end), passport, BigDecimal.valueOf(1000), null);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> clientService.cancelInvoice(invoice2Id, authClient));
            assertEquals(ExceptionReason.INVOICE_ALREADY_STARTED, expectedException.getReason());
        }

        @Test
        @DisplayName("Cancellation successful")
        void cancellationSuccessful() throws DBException, DescriptiveException {
            // Adding driver to invoice
            LimitedUser driverUser = TGenerators.genUser();
            int driverUserId = usersDAO.insertUser(driverUser);
            driverUser.setId(driverUserId);

            int driverId = usersDAO.insertDriver(driverUserId, car.getCity().id());

            invoicesDAO.setInvoiceDriver(invoiceId, driverId);

            // Invoking method
            clientService.cancelInvoice(invoiceId, authClient);

            // Make sure that money has been refunded and invoice has been cancelled
            assertThat(usersDAO.getBalance((Integer) authClient.getId())).isEqualTo(0d);

            InformativeInvoice invoice = invoicesDAO.getInvoiceDetails(invoiceId);
            assertThat(invoice.getStatusList()).contains(InvoiceStatuses.CANCELED);
        }
    }
}