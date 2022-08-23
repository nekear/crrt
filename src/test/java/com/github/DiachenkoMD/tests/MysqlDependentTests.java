package com.github.DiachenkoMD.tests;

import com.github.DiachenkoMD.entities.adapters.DBCoupledAdapter;
import com.github.DiachenkoMD.entities.adapters.LocalDateAdapter;
import com.github.DiachenkoMD.entities.adapters.LocalDateTimeAdapter;
import com.github.DiachenkoMD.entities.adapters.Skip;
import com.github.DiachenkoMD.entities.dto.*;
import com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.InvoicePanelFilters;
import com.github.DiachenkoMD.entities.dto.invoices.PanelInvoice;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.github.DiachenkoMD.entities.enums.CarSegments;
import com.github.DiachenkoMD.entities.enums.Cities;
import com.github.DiachenkoMD.entities.enums.DBCoupled;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.extensions.MysqlExecutionContextExtension;
import com.github.DiachenkoMD.utils.MYSQL_TDatasourceManager;
import com.github.DiachenkoMD.utils.TDBType;
import com.github.DiachenkoMD.utils.TDatabaseManager;
import com.github.DiachenkoMD.utils.TGenerators;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlCarsDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlInvoicesDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlUsersDAO;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.web.services.AdminService;
import com.github.DiachenkoMD.web.services.ManagerService;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.matchers.Or;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * This project contains few situations where we want to have ability to test complicated mysql queries, but we can`t do that
 * with H2, because H2, for example, doesn`t support MATCH AGAINST like mysql do. So I decided to move these methods
 * to the separate class and set up here mysql db (not h2).
 */
@ExtendWith({
        MysqlExecutionContextExtension.class
})
public class MysqlDependentTests {
    private final UsersDAO usersDAO; // with spy
    private final InvoicesDAO invoicesDAO; // with spy
    private final CarsDAO carsDAO; // with spy

    private final Gson gson;

    public MysqlDependentTests(){
        DataSource ds = MYSQL_TDatasourceManager.getDataSource();

        this.usersDAO = spy(new MysqlUsersDAO(ds));
        this.carsDAO = spy(new MysqlCarsDAO(ds));
        this.invoicesDAO = spy(new MysqlInvoicesDAO(ds));

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
    }

    @BeforeAll
    public static void beforeAllSetup(){
        TDatabaseManager.init(MYSQL_TDatasourceManager.getDataSource(), TDBType.MYSQL);
    }

    @BeforeEach
    public void beforeEachSetup(){
        TDatabaseManager.setup();
        TDatabaseManager.destroy();
    }

    @Nested
    @DisplayName("InvoicesDAO")
    class InvoicesDAOTests{
        @Test
        @DisplayName("getPanelInvoicesWithFilters / getPanelInvoicesNumberWithFilters")
        void getPanelInvoicesWithFiltersWithPagination() throws DBException {

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

            // ==> Test case 1: searching by part of randomly generated code with MATCH AGAINST. Awaiting only one entity as the result
            {
                InformativeInvoice invoice = invoicesDAO.getInvoiceDetails(invoice1Id);
                String code = invoice.getCode();
                String partOfTheCode = code.substring(0, 3);

                InvoicePanelFilters invoicePanelFilters = new InvoicePanelFilters();
                invoicePanelFilters.setCode(partOfTheCode);
                var dbRepresentation = invoicePanelFilters.getDBPresentation();
                var orderBy = invoicePanelFilters.getOrderPresentation();
                int limitOffset = 0;
                int limitCount = 1;

                List<PanelInvoice> foundInvoices = invoicesDAO.getPanelInvoicesWithFilters(dbRepresentation, orderBy, limitOffset, limitCount);
                int foundAmount = invoicesDAO.getPanelInvoicesNumberWithFilters(dbRepresentation);

                assertEquals(1, foundInvoices.size());
                assertEquals(1, foundAmount);
            }

            // ==> Test case 2: searching by domain part of client email. Awaiting two entities (because two invoices have the same client) but as we set with 1 el per page => should get 1 el and total amount 2.
            {
                String emailDomain = client.getEmail().split("@")[0].split("\\.")[0]; // test@gmail.com => gmail

                InvoicePanelFilters invoicePanelFilters = new InvoicePanelFilters();
                invoicePanelFilters.setClientEmail(emailDomain);

                var dbRepresentation = invoicePanelFilters.getDBPresentation();
                var orderBy = invoicePanelFilters.getOrderPresentation();
                int limitOffset = 0;
                int limitCount = 1;

                List<PanelInvoice> foundInvoices = invoicesDAO.getPanelInvoicesWithFilters(dbRepresentation, orderBy, limitOffset, limitCount);
                int foundAmount = invoicesDAO.getPanelInvoicesNumberWithFilters(dbRepresentation);

                assertEquals(1, foundInvoices.size());
                assertEquals(2, foundAmount);
            }

        }
    }

    @Nested
    @DisplayName("ManagerService")
    class ManagerServiceTests{

        private final ManagerService managerService;
        private final ServletContext _ctx;

        public ManagerServiceTests(){
            this._ctx = mock(ServletContext.class);

            lenient().when(this._ctx.getAttribute("gson")).thenReturn(gson);

            this.managerService = new ManagerService(usersDAO, invoicesDAO, _ctx);
        }

        @Test
        void getInvoices() throws Exception{
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
                    LocalDate.now().plusDays(4),
                    LocalDate.now().plusDays(10)
            );
            DatesRange datesRange2 = new DatesRange(
                    LocalDate.now().plusDays(12),
                    LocalDate.now().plusDays(20)
            );

            int invoice1Id = invoicesDAO.createInvoice(car1Id, clientId, datesRange, passport, BigDecimal.valueOf(1000), null);
            int invoice2Id = invoicesDAO.createInvoice(car2Id, clientId, datesRange2, passport, BigDecimal.valueOf(1200), driverId);

            // Case 1: finding in dates range from tomorrow to today + 22 days. Pagination is limited to 1 el per page. Should get 1 el and 2 total amount.
            {
                PaginationRequest paginationRequest = new PaginationRequest();
                paginationRequest.setElementsPerPage(1);
                paginationRequest.setAskedPage(1);

                InvoicePanelFilters filters = new InvoicePanelFilters();
                filters.setDatesRange(new DatesRange(LocalDate.now(), LocalDate.now().plusDays(22)));

                paginationRequest.setInvoicesFilters(filters);

                PaginationResponse<PanelInvoice> paginationResponse = managerService.getInvoices(paginationRequest);

                assertThat(paginationResponse.getTotalElements()).isEqualTo(2);
                assertThat(paginationResponse.getResponseData().size()).isEqualTo(1);
            }

            // Case 2: finding by zero filters but ordered descending by price
            {
                PaginationRequest paginationRequest = new PaginationRequest();
                paginationRequest.setElementsPerPage(2);
                paginationRequest.setAskedPage(1);

                InvoicePanelFilters filters = new InvoicePanelFilters();

                Ordery orderByPriceDESC = new Ordery();
                orderByPriceDESC.setName("price");
                orderByPriceDESC.setType("desc");
                filters.setOrderBy(List.of(orderByPriceDESC));

                paginationRequest.setInvoicesFilters(filters);

                PaginationResponse<PanelInvoice> paginationResponse = managerService.getInvoices(paginationRequest);

                assertThat(paginationResponse.getTotalElements()).isEqualTo(2);
                assertThat(paginationResponse.getResponseData().size()).isEqualTo(2);
                assertThat(paginationResponse.getResponseData().get(0).getId()).isEqualTo(invoice2Id); // because second invoice has 1200 price and first - 1000
            }
        }
    }
}
