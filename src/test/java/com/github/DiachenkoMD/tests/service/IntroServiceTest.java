package com.github.DiachenkoMD.tests.service;

import com.github.DiachenkoMD.entities.adapters.DBCoupledAdapter;
import com.github.DiachenkoMD.entities.adapters.LocalDateAdapter;
import com.github.DiachenkoMD.entities.adapters.LocalDateTimeAdapter;
import com.github.DiachenkoMD.entities.adapters.Skip;
import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.invoices.ClientInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.NewRent;
import com.github.DiachenkoMD.entities.dto.invoices.PanelInvoice;
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
import com.github.DiachenkoMD.web.services.IntroService;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.github.DiachenkoMD.web.utils.RightsManager;
import com.github.DiachenkoMD.web.utils.Utils;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletContext;
import javassist.runtime.Desc;
import org.assertj.core.internal.Dates;
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
import java.time.Period;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({
        ConnectionParameterResolverExtension.class,
        DatabaseOperationsExtension.class,
        MockitoExtension.class,
})
class IntroServiceTest {
    private final IntroService introService;
    private final UsersDAO usersDAO; // with spy
    private final InvoicesDAO invoicesDAO; // with spy
    private final CarsDAO carsDAO; // with spy

    private final Gson gson;

    public IntroServiceTest(DataSource ds){
        this.usersDAO = spy(new MysqlUsersDAO(ds));
        this.invoicesDAO = spy(new MysqlInvoicesDAO(ds));
        this.carsDAO = spy(new MysqlCarsDAO(ds));

        ServletContext _ctx = mock(ServletContext.class);

        this.gson = TStore.getGson();

        lenient().when(_ctx.getAttribute("gson")).thenReturn(gson);
        lenient().when(_ctx.getAttribute("rights_manager")).thenReturn(mock(RightsManager.class));

        this.introService = new IntroService(carsDAO, usersDAO, invoicesDAO, _ctx);
    }

    private Car car;
    private LimitedUser client;
    private Passport passport;

    @BeforeEach
    public void beforeEachSetup() throws DBException {
        this.client = TGenerators.genUser();
        int clientId = usersDAO.insertUser(client);
        client.setId(clientId);

        this.passport = TGenerators.genPassport();

        this.car = TGenerators.genCar();
        int carId = carsDAO.create(car);
        this.car.setId(carId);
    }

    @Test
    void getAllCars() throws DBException {
        // Creating second car for that tests
        Car car2 = TGenerators.genCar();
        int car2Id = carsDAO.create(car2);
        car2.setId(car2Id);

        List<Car> foundCars = assertDoesNotThrow(introService::getAllCars);

        assertEquals(2, foundCars.size());

        // Checking that 2 of 2 found cars have the same ids as one, that we have inserted
        int  similarityCounter = 0;

        for (Car foundCar : foundCars) {
            if(foundCar.getId() == car.getId() || foundCar.getId() == car2.getId())
                similarityCounter++;
        }

        assertEquals(2, similarityCounter);
    }

    @Nested
    class getCarsNotRentedInDatesRange{
        @Test
        @DisplayName("Start or end are null [fail]")
        void startOrEndAreNullFail(){
            // Case 1: date range start is null
            {
                DatesRange datesRange = new DatesRange(null, LocalDate.now().plusDays(1));

                assertThrows(IllegalArgumentException.class, () -> introService.getCarsNotRentedInDatesRange(datesRange));
            }

            // Case 2: date range end is null
            {
                DatesRange datesRange = new DatesRange(LocalDate.now(), null);

                assertThrows(IllegalArgumentException.class, () -> introService.getCarsNotRentedInDatesRange(datesRange));
            }
        }

        @Test
        @DisplayName("Acquiring successful")
        void acquiringSuccessful() throws DBException, DescriptiveException {
            // Registering first date
            DatesRange datesRange = new DatesRange(
                    LocalDate.now().minusDays(2), // -2 days
                    LocalDate.now().plusDays(4) // +4 days
            );

            invoicesDAO.createInvoice((Integer) car.getId(), (Integer) client.getId(), datesRange, passport, BigDecimal.valueOf(1000), null);

            // Creating second car for that tests (it should be the only car available, and therefore, returned)
            Car car2 = TGenerators.genCar();
            int car2Id = carsDAO.create(car2);
            car2.setId(car2Id);

            // Getting available cars
            List<String> idsOfCars = introService.getCarsNotRentedInDatesRange(
                    new DatesRange(
                            LocalDate.now().plusDays(1), // +1 day  |
                                                                  //          | <- collides with [-2 days to +4 days]
                            LocalDate.now().plusDays(6) // +6 days  |
                    )
            );

            assertEquals(1, idsOfCars.size());
            assertEquals(car2Id, Integer.parseInt(CryptoStore.decrypt(idsOfCars.get(0))));
        }
    }

    @Test
    void getRentingInfo() throws DBException {
        int carId = (Integer) car.getId();
        DatesRange datesRange = TGenerators.genDatesRange();
        invoicesDAO.createInvoice(carId, (Integer) client.getId(), datesRange, passport, BigDecimal.valueOf(1000), null);

        var rentingInfo = assertDoesNotThrow(() -> introService.getRentingInfo(carId));

        // Make sure, that we acquired dates between original datesRange
        int correctDates = 0;

        int daysBetweenStartAndEnd = Period.between(datesRange.getStart(), datesRange.getEnd()).getDays()+1;
        for (LocalDate ld : rentingInfo.getValue()) {
            Period untilPeriod = ld.until(datesRange.getEnd());
            if(untilPeriod.getDays() <= daysBetweenStartAndEnd && !untilPeriod.isNegative())
                ++correctDates;
        }

        assertEquals(daysBetweenStartAndEnd, correctDates);

        // Make sure, that we have acquired needed car
        assertEquals(carId, rentingInfo.getKey().getId());
    }

    @Test
    void getAvailableDriversOnRange() throws DBException {
        int carId = (Integer) car.getId();

        // Creating available driver, who won't be coupled with any invoice
        LimitedUser driverUser = TGenerators.genUser();
        int driverUserId = usersDAO.insertUser(driverUser);
        driverUser.setId(driverUserId);

        int driverId = usersDAO.insertDriver(driverUserId, car.getCity().id());

        // Creating driver, who will be coupled with one invoice and won`t be available on specified dates range
        LimitedUser secondDriverUser = TGenerators.genUser();
        int secondDriverUserId = usersDAO.insertUser(secondDriverUser);
        secondDriverUser.setId(secondDriverUserId);

        int secondDriverId = usersDAO.insertDriver(secondDriverUserId, car.getCity().id());


        DatesRange datesRange = TGenerators.genDatesRange();
        invoicesDAO.createInvoice(carId, (Integer) client.getId(), datesRange, passport, BigDecimal.valueOf(1000), secondDriverId);

        // Getting drivers ids (should get only first driver)

        List<Integer> availableDriversIds = introService.getAvailableDriversOnRange(
                datesRange.getStart().plusDays(1).format(Utils.localDateFormatter),
                datesRange.getEnd().plusDays(1).format(Utils.localDateFormatter),
                car.getCity().id()
        );

        // Make sure that we get needed driver
        assertEquals(1, availableDriversIds.size());
        assertEquals(driverId, availableDriversIds.get(0));
    }

    @Nested
    class createRent {

        private AuthUser currentUser;

        @BeforeEach
        void init() throws DBException {
            currentUser = usersDAO.get((Integer) client.getId());
        }

        @Test
        @DisplayName("Some dates are null [fail]")
        void someDatesAreNullFail(){
            NewRent newRent = new NewRent();

            newRent.setCarId(car.getId());
            newRent.setPassport(passport);

            // Case 1: dates not set
            newRent.setDatesRange(null);
            DescriptiveException allDatesNullExc = assertThrows(DescriptiveException.class, () -> introService.createRent(gson.toJson(newRent), currentUser));
            assertEquals(ExceptionReason.VALIDATION_ERROR, allDatesNullExc.getReason());

            // Case 2: date start is null
            newRent.setDatesRange(new DatesRange(null, LocalDate.now().plusDays(5)));
            DescriptiveException startDateNullExc = assertThrows(DescriptiveException.class, () -> introService.createRent(gson.toJson(newRent), currentUser));
            assertEquals(ExceptionReason.VALIDATION_ERROR, startDateNullExc.getReason());

            // Case 1: date end is null
            newRent.setDatesRange(new DatesRange(LocalDate.now().plusDays(1), null));
            DescriptiveException endDateNullExc = assertThrows(DescriptiveException.class, () -> introService.createRent(gson.toJson(newRent), currentUser));
            assertEquals(ExceptionReason.VALIDATION_ERROR, endDateNullExc.getReason());
        }

        @Test
        @DisplayName("Bad dates [fail]")
        void badDatesFail(){
            NewRent newRent = new NewRent();

            newRent.setCarId(car.getId());
            newRent.setPassport(passport);

            // Case 1: Date start is before today
            newRent.setDatesRange(new DatesRange(
                    LocalDate.now().minusDays(10),
                    LocalDate.now().plusDays(12)
            ));
            DescriptiveException badStartDateExc = assertThrows(DescriptiveException.class, () -> introService.createRent(gson.toJson(newRent), currentUser));
            assertEquals(ExceptionReason.VALIDATION_ERROR, badStartDateExc.getReason());

            // Case 2: Date end is greater than 2 month gap allowed
            newRent.setDatesRange(new DatesRange(
                    LocalDate.now().plusDays(10),
                    LocalDate.now().plusMonths(2).plusDays(1)
            ));
            DescriptiveException badEndDateExc = assertThrows(DescriptiveException.class, () -> introService.createRent(gson.toJson(newRent), currentUser));
            assertEquals(ExceptionReason.VALIDATION_ERROR, badEndDateExc.getReason());
        }

        @Test
        @DisplayName("Driver not available [fail]")
        // if user selected 'with driver', but driver not available
        void driverNotAvailableFail(){
            NewRent newRent = new NewRent();

            newRent.setCarId(car.getId());
            newRent.setPassport(passport);
            newRent.setDatesRange(TGenerators.genDatesRange());

            newRent.setWithDriver(true);

            DescriptiveException descExc = assertThrows(DescriptiveException.class, () -> introService.createRent(gson.toJson(newRent), currentUser));

            assertEquals(ExceptionReason.DRIVER_NOT_ALLOWED, descExc.getReason());
        }

        @Test
        @DisplayName("Not enough money to pay [fail]")
        void notEnoughMoneyToPayFail(){
            NewRent newRent = new NewRent();

            newRent.setCarId(car.getId());
            newRent.setPassport(passport);
            newRent.setDatesRange(TGenerators.genDatesRange());

            DescriptiveException descExc = assertThrows(DescriptiveException.class, () -> introService.createRent(gson.toJson(newRent), currentUser));

            // Current user balance should be 0
            assertEquals(ExceptionReason.NOT_ENOUGH_MONEY, descExc.getReason());
        }

        @Test
        @DisplayName("Rent creation successful")
        void rentCreationSuccessful() throws DBException {
            int clientId = (Integer) client.getId();


            NewRent newRent = new NewRent();

            newRent.setCarId(car.getId());
            newRent.setPassport(passport);
            newRent.setDatesRange(TGenerators.genDatesRange());
            newRent.setWithDriver(true);

            // Creating driver
            LimitedUser driverUser = TGenerators.genUser();
            int driverUserId = usersDAO.insertUser(driverUser);
            driverUser.setId(driverUserId);
            usersDAO.insertDriver(driverUserId, car.getCity().id());

            // Setting user balance
            usersDAO.setBalance(clientId, (newRent.getDatesRange().getLength().getDays()+1)*car.getPrice());

            assertDoesNotThrow(() -> introService.createRent(gson.toJson(newRent), currentUser));

            // Make user, that user balance is currently null (because we set his balance perfectly for the price of rent)
            assertEquals(0d, usersDAO.getBalance(clientId));

            // Getting invoices on dates range for client
            List<ClientInvoice> foundInvoices = invoicesDAO.getInvoicesForClient(clientId);

            assertEquals(1, foundInvoices.size());
            assertTrue(foundInvoices.get(0).getStatusList().contains(InvoiceStatuses.WITH_DRIVER));
        }
    }
}