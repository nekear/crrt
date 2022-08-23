package com.github.DiachenkoMD.tests.service;

import com.github.DiachenkoMD.entities.adapters.DBCoupledAdapter;
import com.github.DiachenkoMD.entities.adapters.LocalDateAdapter;
import com.github.DiachenkoMD.entities.adapters.LocalDateTimeAdapter;
import com.github.DiachenkoMD.entities.adapters.Skip;
import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.github.DiachenkoMD.entities.enums.*;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.extensions.ConnectionParameterResolverExtension;
import com.github.DiachenkoMD.extensions.DatabaseOperationsExtension;
import com.github.DiachenkoMD.tests.utils.GlobalUtilsTest;
import com.github.DiachenkoMD.utils.TGenerators;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlCarsDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlInvoicesDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlUsersDAO;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.web.services.AdminService;
import com.github.DiachenkoMD.web.services.ManagerService;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.github.DiachenkoMD.web.utils.RightsManager;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletContext;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Note: These tests are integrative.
 */
@ExtendWith({
        ConnectionParameterResolverExtension.class,
        DatabaseOperationsExtension.class,
        MockitoExtension.class,
})
class ManagerServiceTest {

    private final ManagerService managerService;
    private final UsersDAO usersDAO; // with spy
    private final InvoicesDAO invoicesDAO; // with spy
    private final CarsDAO carsDAO; // with spy - external dao for tests (not included into service)

    private final ServletContext _ctx; // mocked

    private final Gson gson;

    public ManagerServiceTest(DataSource ds) {
        this.usersDAO = spy(new MysqlUsersDAO(ds));

        this.invoicesDAO = spy(new MysqlInvoicesDAO(ds));

        this.carsDAO = spy(new MysqlCarsDAO(ds));

        this._ctx = mock(ServletContext.class);

        this.gson = new GsonBuilder()
                .addSerializationExclusionStrategy(new ExclusionStrategy()
                {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f)
                    {
                        return f.getAnnotation(Skip.class) != null;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz)
                    {
                        return false;
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeHierarchyAdapter(DBCoupled.class, new DBCoupledAdapter())
                .create();

        lenient().when(this._ctx.getAttribute("gson")).thenReturn(gson);
        lenient().when(this._ctx.getAttribute("rights_manager")).thenReturn(mock(RightsManager.class));

        this.managerService = new ManagerService(usersDAO, invoicesDAO, _ctx);
    }


    private Car car;
    private LimitedUser client;
    private Passport passport;
    private DatesRange datesRange;
    private int invoiceId;

    @BeforeEach
    public void beforeEachSetup() throws DBException {
        this.client = TGenerators.genUser();
        int clientId = usersDAO.insertUser(client);
        client.setId(clientId);


        this.passport = TGenerators.genPassport();

        this.datesRange = TGenerators.genDatesRange();

        this.car = TGenerators.genCar();
        int carId = carsDAO.create(car);
        this.car.setId(carId);

        // Note: inside createInvoice() method, money are withdrawn from client`s balance. Should be taken into consideration while reviewing tests.
        this.invoiceId = invoicesDAO.createInvoice(carId, clientId, datesRange, passport, BigDecimal.valueOf(1000), null);
    }

    @Test
    void getInvoiceDetails() throws DBException, DescriptiveException {
        // Case 1: getting existing invoice
        {
            String encryptedInvoiceId = CryptoStore.encrypt(String.valueOf(invoiceId));
            InformativeInvoice invoice = managerService.getInvoiceDetails(encryptedInvoiceId);

            assertThat(invoice).isNotNull();
            assertThat(invoice.getId()).isEqualTo(invoiceId);
        }

        // Case 2: getting invoice, which doesn`t exist (awaiting null)
        {
            String encryptedInvoiceId = CryptoStore.encrypt(String.valueOf(123145));
            InformativeInvoice invoice = managerService.getInvoiceDetails(encryptedInvoiceId);

            assertThat(invoice).isNull();
        }
    }

    @Nested
    class createRepairmentInvoice{

        @Test
        @DisplayName("All data correct")
        void allDataCorrect() throws DBException, DescriptiveException {
            ManagerService.CreateRepairmentInvoiceJPC jpc = new ManagerService.CreateRepairmentInvoiceJPC();
            jpc.setOriginId(invoiceId);
            jpc.setPrice(BigDecimal.valueOf(100));
            jpc.setExpirationDate(LocalDate.now().plusMonths(1));
            jpc.setComment("Something");

            InformativeInvoice invoice = managerService.createRepairmentInvoice(gson.toJson(jpc));

            assertEquals(1, invoice.getRepairInvoices().size());
        }


        @ParameterizedTest
        @DisplayName("Failing validation")
        @MethodSource("provideInvalidParameters")
        void validationsFail(Object value, TFailReasons reason){
            ManagerService.CreateRepairmentInvoiceJPC jpc = new ManagerService.CreateRepairmentInvoiceJPC();
            jpc.setOriginId(invoiceId);

            switch (reason){
                case PRICE -> {
                    jpc.setExpirationDate(LocalDate.now().plusDays(100));
                    jpc.setPrice(value != null ? (BigDecimal) value : null);
                }
                case EXP_DATE -> {
                    jpc.setPrice(BigDecimal.valueOf(100));
                    jpc.setExpirationDate(value != null ? (LocalDate) value : null);
                }
            }

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> managerService.createRepairmentInvoice(gson.toJson(jpc)));

            if(value != null && reason == TFailReasons.EXP_DATE){
                assertEquals(ExceptionReason.REP_INVOICE_EXPIRATION_SHOULD_BE_LATER, expectedException.getReason());
            }else{
                assertEquals(ExceptionReason.VALIDATION_ERROR, expectedException.getReason());
            }
        }

        private static Stream<Arguments> provideInvalidParameters() {
            return Stream.of(
                    Arguments.of(
                            null,
                            TFailReasons.PRICE
                    ),
                    Arguments.of(
                            BigDecimal.valueOf(-100),
                            TFailReasons.PRICE
                    ),
                    Arguments.of(
                            null,
                            TFailReasons.EXP_DATE
                    ),
                    Arguments.of(
                            LocalDate.now().minusDays(1),
                            TFailReasons.EXP_DATE
                    )
            );
        }

        enum TFailReasons{
            PRICE, EXP_DATE
        }
    }

    @Test
    void deleteRepairmentInvoice() throws DBException, DescriptiveException {
        int repairmentInvoice = invoicesDAO.createRepairInvoice(invoiceId, BigDecimal.valueOf(1400), LocalDate.now().plusMonths(1), null);
        invoicesDAO.payRepairInvoice(repairmentInvoice); // Note: This method does not withdraw money from the account, but current client balance are not 0, because money are withdrawn at BeforeEach method, while creating invoice

        managerService.deleteRepairmentInvoice(CryptoStore.encrypt(String.valueOf(repairmentInvoice))); // inside, it is expected, that money will be refunded to user

        assertTrue(invoicesDAO.getRepairInvoiceInfo(repairmentInvoice).isEmpty());
        assertEquals(400d, usersDAO.get((Integer) client.getId()).getBalance()); // because originally client had zero balance
    }


    @Nested
    class rejectInvoice{
        String encryptedInvoiceId;

        @BeforeEach
        void nestedInit() throws DescriptiveException {
            encryptedInvoiceId = CryptoStore.encrypt(String.valueOf(invoiceId));
        }

        @Test
        @DisplayName("Invoice already cancelled [fail]")
        void invoiceCancelledFail() throws DBException {
            invoicesDAO.cancelInvoice(invoiceId);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> managerService.rejectInvoice(encryptedInvoiceId, null));
            assertEquals(ExceptionReason.INVOICE_ALREADY_CANCELLED, expectedException.getReason());
        }

        @Test
        @DisplayName("Invoice already rejected [fail]")
        void invoiceRejectedFail() throws DBException {
            invoicesDAO.rejectInvoice(invoiceId, null);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> managerService.rejectInvoice(encryptedInvoiceId, null));
            assertEquals(ExceptionReason.INVOICE_ALREADY_REJECTED, expectedException.getReason());
        }

        @Test
        @DisplayName("Invoice already expired [fail]")
        void invoiceExpiredFail() throws DBException {
            LocalDate start = LocalDate.now().minusDays(10);
            LocalDate end = LocalDate.now().minusDays(1);

            int invoice2Id = invoicesDAO.createInvoice((Integer) car.getId(), (Integer) client.getId(), new DatesRange(start, end), passport, BigDecimal.valueOf(1000), null);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> managerService.rejectInvoice(CryptoStore.encrypt(String.valueOf(invoice2Id)), null));
            assertEquals(ExceptionReason.INVOICE_ALREADY_EXPIRED, expectedException.getReason());
        }

        @Test
        @DisplayName("Invoice already started [fail]")
        void invoiceStartedFail() throws DBException {
            LocalDate start = LocalDate.now().minusDays(1);
            LocalDate end = LocalDate.now().plusDays(4);

            int invoice2Id = invoicesDAO.createInvoice((Integer) car.getId(), (Integer) client.getId(), new DatesRange(start, end), passport, BigDecimal.valueOf(1000), null);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> managerService.rejectInvoice(CryptoStore.encrypt(String.valueOf(invoice2Id)), null));
            assertEquals(ExceptionReason.INVOICE_ALREADY_STARTED, expectedException.getReason());
        }

        @Test
        void correctData() throws DBException, DescriptiveException {
            // Registering driver to check driver notifying module
            LimitedUser driverUser = TGenerators.genUser();
            int driverUserId = usersDAO.insertUser(driverUser);
            driverUser.setId(driverUserId);
            int driverId = usersDAO.insertDriver(driverUserId, car.getCity().id());

            // Setting driver
            invoicesDAO.setInvoiceDriver(invoiceId, driverId);

            managerService.rejectInvoice(encryptedInvoiceId, "Some comment");

            // Verifying that refund was successful
            assertEquals(0, usersDAO.get((Integer) client.getId()).getBalance());

            // Verifying that invoice was rejected
            InformativeInvoice invoice = invoicesDAO.getInvoiceDetails(invoiceId);
            assertThat(invoice.getStatusList()).contains(InvoiceStatuses.REJECTED);
            assertThat(invoice.getRejectionReason()).isEqualTo("Some comment");
        }
    }

    @Test
    void generateInvoicesReport() throws IOException, DBException {
        // Generating some test invoices
        {
            // Generating first car: Ferrari Roma in Kyiv (S-segment)
            Car car1 = new Car();
            car1.setBrand("Ferrari");
            car1.setModel("Roma");
            car1.setCity(Cities.KYIV);
            car1.setSegment(CarSegments.S_SEGMENT);
            car1.setPrice(100d);
            int car1Id = carsDAO.create(car1);
            car1.setId(car1Id);

            // Generating first car: Ford Mustang in Lviv (S-segment)
            Car car2 = new Car();
            car2.setBrand("Ford");
            car2.setModel("Mustang");
            car2.setCity(Cities.LVIV);
            car2.setSegment(CarSegments.S_SEGMENT);
            car2.setPrice(200d);
            int car2Id = carsDAO.create(car2);
            car2.setId(car2Id);

            // Generating user, who will be our client
            LimitedUser client = TGenerators.genUser();
            int clientId = usersDAO.insertUser(client);
            client.setId(clientId);

            // Generating user, who will be our driver
            LimitedUser userDriver = TGenerators.genUser();
            userDriver.setEmail("test@gmail.com");
            int userDriverId = usersDAO.insertUser(userDriver);
            userDriver.setId(userDriverId);
            int driverId = usersDAO.insertDriver(userDriverId, Cities.LVIV.id());

            // Generating random passport and specific datesRange (because we will do further search based on dates)
            Passport passport = TGenerators.genPassport();

            DatesRange datesRange = new DatesRange(
                    LocalDate.of(2022, 10, 10),
                    LocalDate.of(2022, 10, 13)
            );
            DatesRange datesRange2 = new DatesRange(
                    LocalDate.of(2022, 10, 16),
                    LocalDate.of(2022, 10, 20)
            );

            int invoice1Id = invoicesDAO.createInvoice(car1Id, clientId, datesRange, passport, BigDecimal.valueOf(1000), null);
            int invoice2Id = invoicesDAO.createInvoice(car2Id, clientId, datesRange2, passport, BigDecimal.valueOf(1000), driverId);

            // Rejecting first invoice
            invoicesDAO.rejectInvoice(invoice1Id, "Car sent for repair");

            // Adding repair invoices and setting is_paid of the second to "true"
            int repairId1 = invoicesDAO.createRepairInvoice(invoice1Id, BigDecimal.valueOf(200), datesRange.getEnd().plusDays(10), "Repairment invoice comment 1");
            int repairId2 = invoicesDAO.createRepairInvoice(invoice1Id, BigDecimal.valueOf(400), datesRange.getEnd().plusDays(5), "Repairment invoice comment 2 (should be paid)");

            invoicesDAO.payRepairInvoice(repairId2);
        }

        // Generating report

        // Generating report
        Workbook workbook = managerService.generateInvoicesReport();

        // Checking for needed amount of sheets and last row index
        assertEquals(3, workbook.getNumberOfSheets());

        Sheet invoicesSheet = workbook.getSheetAt(0);
        assertEquals(3, invoicesSheet.getLastRowNum());

        Sheet passportSheet = workbook.getSheetAt(1);
        assertEquals(3, passportSheet.getLastRowNum());

        Sheet repairInvoicesSheet = workbook.getSheetAt(2);
        assertEquals(2, repairInvoicesSheet.getLastRowNum());

//        UNCOMMENT THIS IF YOU WANT TO CHECK HOW FINAL REPORT WOULD LOOK LIKE
//        File currDir = new File(".");
//        String path = currDir.getAbsolutePath();
//        String fileLocation = path.substring(0, path.length() - 1) + "reportTmp.xlsx";
//
//        FileOutputStream outputStream = new FileOutputStream(fileLocation);
//        workbook.write(outputStream);

        workbook.close();
    }
}