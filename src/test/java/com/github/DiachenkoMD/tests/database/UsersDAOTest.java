package com.github.DiachenkoMD.tests.database;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.drivers.ExtendedDriver;
import com.github.DiachenkoMD.entities.dto.drivers.LimitedDriver;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.dto.users.PanelUser;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.github.DiachenkoMD.entities.enums.AccountStates;
import com.github.DiachenkoMD.entities.enums.CarSegments;
import com.github.DiachenkoMD.entities.enums.Cities;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlCarsDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlInvoicesDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlUsersDAO;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.extensions.ConnectionParameterResolverExtension;
import com.github.DiachenkoMD.extensions.DatabaseOperationsExtension;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith({
        DatabaseOperationsExtension.class,
        ConnectionParameterResolverExtension.class
})
class UsersDAOTest {

    private final UsersDAO usersDAO;
    private final InvoicesDAO invoicesDAO;
    private final CarsDAO carsDAO;

    public UsersDAOTest(DataSource ds){
        this.usersDAO = new MysqlUsersDAO(ds);
        this.invoicesDAO = new MysqlInvoicesDAO(ds);
        this.carsDAO = new MysqlCarsDAO(ds);
    }

    @Test
    @DisplayName("get(email)")
    void getUserByEmailTest() throws Exception {
        AuthUser fakeUser = AuthUser.of("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

        usersDAO.completeRegister(fakeUser, "random_pass");

        AuthUser acquiredUser = usersDAO.get(fakeUser.getEmail());

        assertNotNull(fakeUser.getId());
        assertEquals(fakeUser.getFirstname(), acquiredUser.getFirstname());
        assertEquals(fakeUser.getSurname(), acquiredUser.getSurname());
        assertEquals(fakeUser.getPatronymic(), acquiredUser.getPatronymic());
        assertNotNull(fakeUser.getConfirmationCode()); // thats value is generated so we have to set it

        assertNotNull(acquiredUser.getConfirmationCode());
    }

    @Test
    @DisplayName("get(userId)")
    void getUserByUserIdTest() throws Exception {
        LimitedUser fakeUser = new LimitedUser();
        fakeUser.setEmail("test@gmail.com");

        usersDAO.register(fakeUser, "random_pass");

        AuthUser acquiredUser = usersDAO.get((Integer) fakeUser.getId());

        assertNotNull(acquiredUser);
        assertEquals((Integer) fakeUser.getId(), (Integer)  acquiredUser.getId());
    }

    @Test
    @DisplayName("getAll")
    void getAllUsersTest() throws Exception {
        assertEquals(usersDAO.getAll().size(), 0);

        AuthUser newUser = AuthUser.of("test@gmail.com", "Firstname", "Surname", "Patronymic");
        AuthUser newUser2 = AuthUser.of("test2@gmail.com", "Firstname", "Surname", "Patronymic");

        usersDAO.register(newUser, "random_pass");
        usersDAO.register(newUser2, "random_pass");

        assertEquals(2, usersDAO.getAll().size());
    }

    @Test
    @DisplayName("getFromDriver")
    void getFromDriverTest() throws Exception{
        // Registering new user
        LimitedUser limitedUser = new LimitedUser();
        limitedUser.setEmail("test@gmail.com");
        usersDAO.register(limitedUser, "hello123");

        // Inserting driver
        int newDriverId = usersDAO.insertDriver((Integer) limitedUser.getId(), Cities.KYIV.id());

        // Awaiting acquiring the same user id, as was registered
        LimitedUser driver = usersDAO.getFromDriver(newDriverId).get();
        assertEquals(limitedUser.getEmail(), driver.getEmail());
    }

    @Test
    @DisplayName("getDriverFromUser")
    void getDriverFromUserTest() throws Exception{
        // Registering new user
        LimitedUser limitedUser = new LimitedUser();
        limitedUser.setEmail("test@gmail.com");
        usersDAO.register(limitedUser, "hello123");

        // Inserting driver
        int newDriverId = usersDAO.insertDriver((Integer) limitedUser.getId(), Cities.KYIV.id());

        // Looking for driver in db
        ExtendedDriver driver = usersDAO.getDriverFromUser((Integer) limitedUser.getId()).get();

        // Inserted driver id should equal to found driver id
        assertEquals(newDriverId, (Integer) driver.getId());
    }


    @Nested
    @DisplayName("register")
    class registerTests{
        @Test
        @DisplayName("Create new user")
        void testCreateNewUser() throws Exception {
            AuthUser newUser = AuthUser.of("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

            assertInstanceOf(Integer.class, usersDAO.register(newUser, "1").getId());
        }

        @Test
        @DisplayName("Create new user without credentials")
        void testCreateNewUserWithoutCredentials() throws Exception {
            AuthUser newUser = AuthUser.of("test@gmail.com", null, null, null);

            Integer id = (Integer) usersDAO.register(newUser, "password1").getId();

            AuthUser foundUser = usersDAO.get(newUser.getEmail());


            // Checking whether insertion was correct
            assertNotNull(foundUser);

            // Checking for not having credentials set
            assertNull(foundUser.getFirstname());
            assertNull(foundUser.getSurname());
            assertNull(foundUser.getPatronymic());
            assertNull(foundUser.getConfirmationCode());
        }
    }

    @Nested
    @DisplayName("completeRegister")
    class completeRegisterTests{
        @Test
        @DisplayName("Create new user")
        void testCreateNewUser() throws Exception {
            AuthUser newUser = AuthUser.of("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

            AuthUser created = usersDAO.completeRegister(newUser, "password1");

            assertDoesNotThrow(() -> created.getCleanId().get());

            assertNotNull(created.getConfirmationCode());
        }

        @Test
        @DisplayName("Create new user without credentials")
        void testCreateNewUserWithoutCredentials() throws Exception {
            AuthUser newUser = AuthUser.of("test@gmail.com", null, null, null);

            assertNotNull(usersDAO.completeRegister(newUser, "password1"));
                        
            // Checking whether insertion was correct
            assertNotNull(newUser.getCleanId().get());

            // Checking for not having credentials set
            assertNull(newUser.getFirstname());
            assertNull(newUser.getSurname());
            assertNull(newUser.getPatronymic());

            // Checking for code being generated
            assertNotNull(newUser.getConfirmationCode());
        }
    }

    @Nested
    @DisplayName("doesExist")
    class doesExistTests{
        @Test
        @DisplayName("Should find nothing")
        void testUserShouldntExist() throws Exception {
            AuthUser testUser = AuthUser.of("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

            assertFalse(usersDAO.doesExist(testUser));
        }
        @Test
        @DisplayName("Should find one")
        void testUserShouldExist() throws Exception {
            AuthUser testUser = AuthUser.of("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

            usersDAO.register(testUser, "1");

            assertTrue(usersDAO.doesExist(testUser));
        }
    }

    @Test
    @DisplayName("generateConfirmationCode")
    void generateUserCodeTest() throws Exception {
        String generatedCode = usersDAO.generateConfirmationCode();
        assertNotNull(generatedCode);
        assertNull(usersDAO.getUserByConfirmationCode(generatedCode));
    }

    @Test
    @DisplayName("Overall get tests")
    void overallGetTests() throws Exception{
        // Registering new user entity
        AuthUser registered = usersDAO.completeRegister(AuthUser.of("email@gmail.com", null, null, null), "password1");

        // Testing get password
        assertEquals("password1", usersDAO.getPassword(registered.getCleanId().get()));

        // Testing get confirmation code
        assertEquals(registered.getEmail(), usersDAO.getUserByConfirmationCode(registered.getConfirmationCode()).getEmail());

        // Testing get balance
        assertEquals(registered.getBalance(), usersDAO.getBalance(registered.getCleanId().get()));

        // Testing get avatar
        assertTrue(usersDAO.getAvatar(registered.getCleanId().get()).isEmpty());
    }
    @Test
    @DisplayName("Overall set tests") // except setUserState
    void overallSetTests() throws Exception{
        // Registering new user entity
        LimitedUser registered = usersDAO.register(AuthUser.of("email@gmail.com", null, null, null), "password1");

        // Testing password set
        usersDAO.setPassword(registered.getCleanId().get(), "password2");
        assertEquals("password2", usersDAO.getPassword(registered.getCleanId().get()));

        // Testing confirmation set code
        usersDAO.setConfirmationCode(registered.getCleanId().get(), "confirmationCode1");
        assertEquals(registered.getEmail(), usersDAO.getUserByConfirmationCode("confirmationCode1").getEmail());

        // Testing set balance
        usersDAO.setBalance(registered.getCleanId().get(), 10);
        assertEquals(10, usersDAO.getBalance(registered.getCleanId().get()));

        //Testing set avatar
        usersDAO.setAvatar(registered.getCleanId().get(), "avatarFile.jpg");
        assertEquals("avatarFile.jpg", usersDAO.getAvatar(registered.getCleanId().get()).get());

        //Testing set driver city
        int newDriverId = usersDAO.insertDriver((Integer) registered.getId(), Cities.KYIV.id());
        usersDAO.setDriverCity(newDriverId, Cities.LVIV.id());

        assertEquals(Cities.LVIV, usersDAO.getDriverFromUser((Integer) registered.getId()).get().getCity());
    }

    @Test
    @DisplayName("updateUsersData")
    void updateUsersDataTest() throws Exception{
        // Registering new user entity
        LimitedUser registered = usersDAO.register(AuthUser.of("email@gmail.com", null, null, null), "password1");

        String firstname = "Mykhailo";
        String surname = "Diachenko";
        String patronymic = "Dmytrovich";


        // Setting firstname, surname and patronymic
        assertTrue(usersDAO.updateUsersData(registered.getCleanId().get(), new HashMap<>(
                Map.of(
                        "firstname", firstname,
                        "surname", surname,
                        "patronymic", patronymic
                )
        )));

        // Checks
        AuthUser acquiredFromDBUser = usersDAO.get(registered.getEmail());

        assertEquals(firstname, acquiredFromDBUser.getFirstname());
        assertEquals(surname, acquiredFromDBUser.getSurname());
        assertEquals(patronymic, acquiredFromDBUser.getPatronymic());
    }

    @Test
    @DisplayName("getUsersWithFilters")
    void getUsersWithFiltersTest() throws Exception{
        List<LimitedUser> usersList = getRandomUsersList();

        usersList.stream().forEach(x -> {
            try {
                x.setId(usersDAO.insertUser(x));
            } catch (DBException e) {
                throw new RuntimeException(e);
            }
        });

        HashMap<String, String> filters = new HashMap<>(
                Map.of(
                        "email", "%test%"
                )
        );

        List<PanelUser> foundUsers = usersDAO.getUsersWithFilters(filters, 0, 15);

        assertEquals(2, foundUsers.size());


        // Checking whether we found exactly the same users as we inserted
        int foundCounter = 0;

        for(PanelUser foundUser : foundUsers){
            if(foundUser.getEmail().equals(usersList.get(0).getEmail()) || foundUser.getEmail().equals(usersList.get(1).getEmail()))
                foundCounter++;
        }

        assertEquals(2, foundCounter);
    }

    @Test
    @DisplayName("getUsersNumberWithFilters")
    void getUsersNumberWithFiltersTest() throws Exception{
        List<LimitedUser> usersList = getRandomUsersList();

        usersList.stream().forEach(x -> {
            try {
                x.setId(usersDAO.insertUser(x));
            } catch (DBException e) {
                throw new RuntimeException(e);
            }
        });

        HashMap<String, String> filters = new HashMap<>(
                Map.of(
                        "email", "%test%"
                )
        );

        int amount = usersDAO.getUsersNumberWithFilters(filters);

        assertEquals(2, amount);
    }

    private static List<LimitedUser> getRandomUsersList(){
        LimitedUser user1 = new LimitedUser();
        user1.setEmail("test@gmail.com");

        LimitedUser user2 = new LimitedUser();
        user2.setEmail("notliketest@mail.ua");

        LimitedUser user3  = new LimitedUser();
        user3.setEmail("john@doe.uk");
        user3.setFirstname("Михайло");

        return List.of(user1, user2, user3);
    }

    @DisplayName("setUserState")
    @Test
    void setUserStateTest() throws Exception{
        LimitedUser user = new LimitedUser();
        user.setEmail("some@gmail.com");

        user.setId(usersDAO.insertUser(user));

        assertEquals(user.getState(), AccountStates.UNBLOCKED);

        assertTrue(usersDAO.setUserState((Integer) user.getId(), AccountStates.BLOCKED.id()));

        assertEquals(AccountStates.BLOCKED, usersDAO.get((Integer) user.getId()).getState());
    }

    @DisplayName("deleteUsers")
    @Test
    void deleteUsersTest() throws Exception{
        LimitedUser user = new LimitedUser();
        user.setEmail("some@gmail.com");
        user.setId(usersDAO.insertUser(user));

        LimitedUser user2 = new LimitedUser();
        user2.setEmail("some2@gmail.com");
        user2.setId(usersDAO.insertUser(user2));

        assertEquals(2, usersDAO.getAll().size());

        usersDAO.deleteUsers(List.of((Integer) user.getId(), (Integer) user2.getId()));

        assertEquals(0, usersDAO.getAll().size());
    }

    @DisplayName("getAvailableDriversOnRange")
    @Nested
    class getAvailableDriversOnRangeTests{

        private LimitedUser user;
        private ExtendedDriver driver;

        // Invoice from 2022-08-18 to 2022-09-19 is created in this method
        @BeforeEach
        public void setup() throws DBException {
            user = new LimitedUser();
            user.setEmail("test@gmail.com");
            user.setId(usersDAO.insertUser(user));

            int driverId = usersDAO.insertDriver((Integer) user.getId(), Cities.KYIV.id());

            driver = usersDAO.getDriverFromUser((Integer) user.getId()).get();

            Passport passport = new Passport();

            passport.setFirstname("A");
            passport.setSurname("B");
            passport.setPatronymic("C");
            passport.setDateOfIssue(LocalDate.now());
            passport.setDateOfBirth(LocalDate.now());
            passport.setDocNumber(21312323);
            passport.setRntrc(4242134);
            passport.setAuthority(4243);


            Car car = new Car();

            car.setBrand("Audi");
            car.setModel("G7");
            car.setPrice(100.0);
            car.setCity(Cities.KYIV);
            car.setSegment(CarSegments.S_SEGMENT);

            car.setId(carsDAO.create(car));

            invoicesDAO.createInvoice(
                    (Integer) car.getId(),
                    (Integer) user.getId(),
                    new DatesRange(
                            LocalDate.of(2022, Month.AUGUST, 18),
                            LocalDate.of(2022, Month.SEPTEMBER, 19)
                    ),
                    passport,
                    BigDecimal.valueOf(1000),
                    (Integer) driver.getId()
            );

        }

        @Test
        void shouldNotGetDriverOnFilledDates() throws Exception{
            assertEquals(0,
                    usersDAO.getAvailableDriversOnRange(
                        LocalDate.of(2022, Month.AUGUST, 14),
                        LocalDate.of(2022, Month.AUGUST, 28),
                        driver.getCity().id()
                    ).size()
            );
        }

        @Test
        void shouldGetDriverOnEmptyTimeline() throws Exception{
            assertEquals(1,
                    usersDAO.getAvailableDriversOnRange(
                            LocalDate.of(2022, Month.SEPTEMBER, 20),
                            LocalDate.of(2022, Month.SEPTEMBER, 25),
                            driver.getCity().id()
                    ).size()
            );
        }
    }

}
