package com.github.DiachenkoMD.tests.service;

import com.github.DiachenkoMD.entities.adapters.DBCoupledAdapter;
import com.github.DiachenkoMD.entities.adapters.LocalDateAdapter;
import com.github.DiachenkoMD.entities.adapters.LocalDateTimeAdapter;
import com.github.DiachenkoMD.entities.adapters.Skip;
import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.drivers.ExtendedDriver;
import com.github.DiachenkoMD.entities.dto.invoices.DriverInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.github.DiachenkoMD.entities.enums.Cities;
import com.github.DiachenkoMD.entities.enums.DBCoupled;
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
import com.github.DiachenkoMD.web.services.DriverService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({
        ConnectionParameterResolverExtension.class,
        DatabaseOperationsExtension.class,
        MockitoExtension.class,
})
class DriverServiceTest {

    private final DriverService driverService;
    private final UsersDAO usersDAO; // with spy
    private final InvoicesDAO invoicesDAO; // with spy
    private final CarsDAO carsDAO; // with spy

    public DriverServiceTest(DataSource ds){
        this.usersDAO = spy(new MysqlUsersDAO(ds));
        this.invoicesDAO = spy(new MysqlInvoicesDAO(ds));
        this.carsDAO = spy(new MysqlCarsDAO(ds));

        // mocked
        ServletContext _ctx = mock(ServletContext.class);

        Gson gson = TStore.getGson();

        lenient().when(_ctx.getAttribute("gson")).thenReturn(gson);
        lenient().when(_ctx.getAttribute("rights_manager")).thenReturn(mock(RightsManager.class));

        this.driverService = new DriverService(usersDAO, invoicesDAO, _ctx);
    }

    private Car car;
    private LimitedUser client;
    private LimitedUser driverUser;

    private int driverId;

    private Passport passport;
    private int invoiceId;

    @BeforeEach
    public void beforeEachSetup() throws DBException {
        this.client = TGenerators.genUser();
        int clientId = usersDAO.insertUser(client);
        this.client.setId(clientId);

        this.driverUser = TGenerators.genUser();
        int driverUserId = usersDAO.insertUser(driverUser);
        this.driverUser.setId(driverUserId);

        this.passport = TGenerators.genPassport();

        DatesRange datesRange = TGenerators.genDatesRange();

        this.car = TGenerators.genCar();
        int carId = carsDAO.create(car);
        this.car.setId(carId);

        this.driverId = usersDAO.insertDriver(driverUserId, car.getCity().id());

        this.invoiceId = invoicesDAO.createInvoice(carId, clientId, datesRange, passport, BigDecimal.valueOf(1000), driverId);
    }

    @Test
    void getInvoices() throws DBException {
        List<DriverInvoice> driverInvoices = driverService.getInvoices((Integer) driverUser.getId());

        assertEquals(1, driverInvoices.size());
        assertEquals(invoiceId, driverInvoices.get(0).getId());
    }

    @Nested
    class skipInvoice {

        private AuthUser authDriver;

        @BeforeEach
        void init() throws DBException {
            authDriver = usersDAO.get((Integer) driverUser.getId());
        }

        @Test
        @DisplayName("Not enough rights to skip [fail]")
        void notEnoughRightToSkipFail() throws DBException {
            // Creating another driver to check access denying
            LimitedUser secondDriverUser = TGenerators.genUser();
            int secondDriverUserId = usersDAO.insertUser(secondDriverUser);
            secondDriverUser.setId(secondDriverUserId);

            int secondDriverId = usersDAO.insertDriver(secondDriverUserId, car.getCity().id());

            DescriptiveException expException = assertThrows(DescriptiveException.class, () -> driverService.skipInvoice(invoiceId, usersDAO.get(secondDriverUserId)));

            assertEquals(ExceptionReason.ACCESS_DENIED, expException.getReason());
        }

        @Test
        @DisplayName("Invoice already rejected [fail]")
        void invoiceRejectedFail() throws DBException {
            invoicesDAO.rejectInvoice(invoiceId, null);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> driverService.skipInvoice(invoiceId, authDriver));
            assertEquals(ExceptionReason.ACCESS_DENIED, expectedException.getReason());
        }

        @Test
        @DisplayName("Invoice already cancelled [fail]")
        void invoiceCancelledFail() throws DBException {
            invoicesDAO.cancelInvoice(invoiceId);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> driverService.skipInvoice(invoiceId, authDriver));
            assertEquals(ExceptionReason.ACCESS_DENIED, expectedException.getReason());
        }

        @Test
        @DisplayName("Invoice already expired [fail]")
        void invoiceExpiredFail() throws DBException {
            LocalDate start = LocalDate.now().minusDays(10);
            LocalDate end = LocalDate.now().minusDays(1);

            int invoice2Id = invoicesDAO.createInvoice((Integer) car.getId(), (Integer) client.getId(), new DatesRange(start, end), passport, BigDecimal.valueOf(1000), driverId);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> driverService.skipInvoice(invoice2Id, authDriver));
            assertEquals(ExceptionReason.ACCESS_DENIED, expectedException.getReason());
        }

        @Test
        @DisplayName("Invoice already started [fail]")
        void invoiceStartedFail() throws DBException {
            LocalDate start = LocalDate.now().minusDays(1);
            LocalDate end = LocalDate.now().plusDays(4);

            int invoice2Id = invoicesDAO.createInvoice((Integer) car.getId(), (Integer) client.getId(), new DatesRange(start, end), passport, BigDecimal.valueOf(1000), driverId);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> driverService.skipInvoice(invoice2Id, authDriver));
            assertEquals(ExceptionReason.ACCESS_DENIED, expectedException.getReason());
        }


        @Test
        @DisplayName("Delegation successful")
        void delegationSuccessful() throws DBException {
            // Creating another driver to be able to skip
            LimitedUser secondDriverUser = TGenerators.genUser();
            int secondDriverUserId = usersDAO.insertUser(secondDriverUser);
            secondDriverUser.setId(secondDriverUserId);

            int secondDriverId = usersDAO.insertDriver(secondDriverUserId, car.getCity().id());

            // Delegating to another driver
            assertTrue(assertDoesNotThrow(() -> driverService.skipInvoice(invoiceId, authDriver)));

            // Checking that driver changed
            InformativeInvoice invoice = invoicesDAO.getInvoiceDetails(invoiceId);

            assertEquals(invoice.getDriver().getEmail(), secondDriverUser.getEmail());
        }
    }

    @Nested
    class changeCity {
        @Test
        @DisplayName("New city id == current city [nothing]")
        void newCityIsTheSame() throws DBException {
            assertDoesNotThrow(() -> driverService.changeCity((Integer) driverUser.getId(), car.getCity()));

            verify(usersDAO, never()).setDriverCity(anyInt(), anyInt());
        }

        @Test
        @DisplayName("City update successful")
        void cityUpdatedSuccessful() throws DBException {
            int driverUserId = (Integer) driverUser.getId();

            assertDoesNotThrow(() -> driverService.changeCity(driverUserId, Cities.LVIV));

            ExtendedDriver driverInfo = assertDoesNotThrow(() -> usersDAO.getDriverFromUser(driverUserId)).get();

            assertEquals(driverInfo.getCity(), Cities.LVIV);
        }
    }

    @Nested
    class getCity {

        @Test
        @DisplayName("Get city of user (not driver) [fail]")
        void getCityOfNonDriverUserFail(){
            int clientId = (Integer) client.getId();

            DescriptiveException expException = assertThrows(DescriptiveException.class, () -> driverService.getCity(clientId));
            assertEquals(ExceptionReason.ACQUIRING_ERROR, expException.getReason());
        }

        @Test
        @DisplayName("Get city of existing driver")
        void getCityOfDriver(){
            int driverUserId = (Integer) driverUser.getId();

            assertEquals(assertDoesNotThrow(() -> driverService.getCity(driverUserId)), car.getCity());
        }
    }
}