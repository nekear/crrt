package com.github.DiachenkoMD.tests.service;

import com.github.DiachenkoMD.entities.adapters.DBCoupledAdapter;
import com.github.DiachenkoMD.entities.adapters.LocalDateAdapter;
import com.github.DiachenkoMD.entities.adapters.LocalDateTimeAdapter;
import com.github.DiachenkoMD.entities.adapters.Skip;
import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.PaginationRequest;
import com.github.DiachenkoMD.entities.dto.PaginationResponse;
import com.github.DiachenkoMD.entities.dto.users.*;
import com.github.DiachenkoMD.entities.enums.AccountStates;
import com.github.DiachenkoMD.entities.enums.DBCoupled;
import com.github.DiachenkoMD.entities.enums.Roles;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.extensions.ConnectionParameterResolverExtension;
import com.github.DiachenkoMD.extensions.DatabaseOperationsExtension;
import com.github.DiachenkoMD.utils.TGenerators;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlCarsDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlInvoicesDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlUsersDAO;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.web.services.AdminService;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.github.DiachenkoMD.web.utils.RightsManager;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * Note: Unlike {@link UserServiceTest}, tests of {@link AdminService} are integrational.
 */
@ExtendWith({
        ConnectionParameterResolverExtension.class,
        DatabaseOperationsExtension.class,
        MockitoExtension.class,
})
class AdminServiceTest {

    private final AdminService adminService;
    private final UsersDAO usersDAO; // with spy
    private final InvoicesDAO invoicesDAO; // with spy
    private final CarsDAO carsDAO; // with spy

    private final ServletContext _ctx; // mocked

    private final Gson gson;

    public AdminServiceTest(DataSource ds) {
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

        this.adminService = new AdminService(usersDAO, carsDAO, invoicesDAO, _ctx);
    }

    @Test
    void getStats() throws DBException {
        assertThat(adminService.getStats().size()).isEqualTo(3);
        verify(invoicesDAO).getStats();
    }

    @Test
    void getCars() throws DBException {
        assertNotNull(adminService.getCars());
        verify(carsDAO).getAll();
    }

    @Test
    void getCar() throws DBException, DescriptiveException {
        Car car = TGenerators.genCar();
        int carId = carsDAO.create(car);

        Car acquiredFromService = adminService.getCar(carId);

        assertThat(acquiredFromService.getId()).isEqualTo(carId);
    }

    @Test
    void createCar(@TempDir Path tempDir) throws Exception {
        String imageName = "image.jpg"; // let`s image, that we are uploading that file with car

        Car car = TGenerators.genCar();

        HttpServletRequest _req = mock(HttpServletRequest.class);
        Part _carJSONPart  = mock(Part.class);
        when(_carJSONPart.getName()).thenReturn("document");

        // For faking file writing of the car image
        Part _image  = mock(Part.class);
        when(_image.getSubmittedFileName()).thenReturn(imageName);
        when(_image.getName()).thenReturn("carImage");

        // For faking other necessary method calls during execution
        when(_req.getParts()).thenReturn(List.of(_carJSONPart, _image));
        when(_req.getPart("document")).thenReturn(_carJSONPart);
        when(_carJSONPart.getInputStream()).thenReturn(new ByteArrayInputStream(gson.toJson(car).getBytes(StandardCharsets.UTF_8)));

        when(_ctx.getRealPath(anyString())).thenReturn(tempDir.toAbsolutePath().toString()); // faking file writing path


        assertDoesNotThrow(() -> adminService.createCar(_req));

        List<Car> createdCars = carsDAO.getAll();
        assertEquals(1, createdCars.size());

        Car createdCar = createdCars.get(0);
        assertEquals(1, carsDAO.get((Integer) createdCar.getId()).get().getImages().size()); // getting images from db, because images are omitted on Cars from getAll()

        // Checking for whether program written file to folder successfully
        verify(_image).write(anyString());
    }

    @Test
    void addImageToCar() throws Exception {
        Car car = TGenerators.genCar();
        int carId = carsDAO.create(car);
        String encryptedCarId = CryptoStore.encrypt(String.valueOf(carId));

        HttpServletRequest _req = mock(HttpServletRequest.class);
        Part _carInfoJSON  = mock(Part.class);
        Part _image = mock(Part.class);

        // Faking behavior of data parts
        when(_req.getPart("document")).thenReturn(_carInfoJSON);
        when(_req.getPart("car-image")).thenReturn(_image);
        // Faking file name for method, that generated unique file name
        when(_image.getSubmittedFileName()).thenReturn("image.jpg");
        // Faking car id to be able to add image to car
        when(_carInfoJSON.getInputStream()).thenReturn(new ByteArrayInputStream(gson.toJson(Map.of("car_id", encryptedCarId)).getBytes(StandardCharsets.UTF_8)));

        assertDoesNotThrow(() -> adminService.addImageToCar(_req));

        assertEquals(1, carsDAO.get(carId).get().getImages().size());
    }

    @Test
    void deleteImageFromCar(@TempDir Path tempDir) throws Exception {
        String imageName = "some.jpg";

        Car car = TGenerators.genCar();
        int carId = carsDAO.create(car);
        car.setId(carId);

        int imageId = carsDAO.addImage(carId, imageName);
        String imageIdEncrypted = CryptoStore.encrypt(String.valueOf(imageId));

        HttpServletRequest _req = mock(HttpServletRequest.class);

        // Creating tmp file for bypassing file deletion method
        tempDir.resolve(imageName).toFile().createNewFile();

        // Faking behavior of main methods
        when(_ctx.getRealPath(anyString())).thenReturn(tempDir.toAbsolutePath().toString());
        when(_req.getReader()).thenReturn(
                new BufferedReader(
                    new InputStreamReader(
                            new ByteArrayInputStream(gson.toJson(Map.of("id", imageIdEncrypted)).getBytes(StandardCharsets.UTF_8))
                    )
                )
        );

        assertDoesNotThrow(() -> adminService.deleteImageFromCar(_req));
        assertNull(carsDAO.get(carId).get().getImages());
    }

    @Test
    void updateCar() throws Exception {
        Car car = TGenerators.genCar();
        int carId = carsDAO.create(car);
        car.setId(carId);

        // Changing car data to check execution of update
        car.setBrand("Ferrari");
        car.setModel("Roma");

        HttpServletRequest _req = mock(HttpServletRequest.class);

        // [FAIL] case 1: incoming json is empty
        {
            when(_req.getReader()).thenReturn(
                    new BufferedReader(
                            new InputStreamReader(
                                    new ByteArrayInputStream("".getBytes())
                            )
                    )
            );

            assertThrows(DescriptiveException.class, () -> adminService.updateCar(_req));
        }

        // [PASS] case 2: data is correct
        {
            when(_req.getReader()).thenReturn(
                    new BufferedReader(
                            new InputStreamReader(
                                    new ByteArrayInputStream(gson.toJson(car).getBytes(StandardCharsets.UTF_8))
                            )
                    )
            );

            assertDoesNotThrow(() -> adminService.updateCar(_req));

            assertThat(carsDAO.get(carId).get()).usingRecursiveComparison().isEqualTo(car);
        }
    }

    @Test
    void deleteCar(@TempDir Path tempDir) throws Exception {
        Car car = TGenerators.genCar();
        int carId = carsDAO.create(car);
        car.setId(carId);
        String carIdEncrypted = CryptoStore.encrypt(String.valueOf(carId));

        HttpServletRequest _req = mock(HttpServletRequest.class);
        when(_req.getReader()).thenReturn(
                new BufferedReader(
                        new InputStreamReader(
                                new ByteArrayInputStream(gson.toJson(Map.of("id", carIdEncrypted)).getBytes(StandardCharsets.UTF_8))
                        )
                )
        );

        // Special mocking stage for testing images deletion on car deletion
        String imageName = "some.jpg";
        carsDAO.addImage(carId, imageName);

        tempDir.resolve(imageName).toFile().createNewFile(); // creating new file to further delete it inside service method
        when(_ctx.getRealPath(anyString())).thenReturn(tempDir.toAbsolutePath().toString());

        // Executing checks
        assertDoesNotThrow(() -> adminService.deleteCar(_req));
        assertTrue(carsDAO.get(carId).isEmpty());
        assertEquals(0, tempDir.toFile().listFiles().length); // check whether file was correctly delted or not
    }

    @Test
    void getUsers() throws Exception{
        // Preparing db for tests and creating two users
        LimitedUser user1 = TGenerators.genUser();
        LimitedUser user2 = TGenerators.genUser();

        user1.setEmail("test@gmail.com");
        user2.setEmail("some@gmail.com");

        int user1Id = usersDAO.insertUser(user1);
        int user2Id = usersDAO.insertUser(user2);

        user1.setId(user1Id);
        user2.setId(user2Id);

        // Creating filters object
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setAskedPage(1);
        paginationRequest.setElementsPerPage(1);

        UsersPanelFilters filters = new UsersPanelFilters();
        filters.setEmail("gmail");

        paginationRequest.setUsersFilters(filters);

        // Executing service method and awaiting 1 element and 2 totalAmount
        PaginationResponse<PanelUser> foundUsers = adminService.getUsers(gson.toJson(paginationRequest));

        assertEquals(1, foundUsers.getResponseData().size());
        assertEquals(2, foundUsers.getTotalElements());
    }

    @Nested
    @DisplayName("createUser")
    class createUserTests{

        @Test
        @DisplayName("Creation successful")
        void creationSuccessful() throws Exception{
            AdminService.CreationUpdatingUserJPC user = new AdminService.CreationUpdatingUserJPC();
            user.setEmail("test@gmail.com");
            user.setFirstname("Mykhailo");
            user.setSurname("Diachneko");
            user.setPatronymic("Dmytrovich");
            user.setPassword("password123");
            user.setRole(Roles.MANAGER);

            adminService.createUser(gson.toJson(user));

            assertNotNull(usersDAO.get("test@gmail.com"));
        }

        @ParameterizedTest
        @DisplayName("Failing validation")
        @MethodSource("provideInvalidUserParameters")
        void testUserDataFailValidation(String value, TValidationCategories category) throws Exception {
            AdminService.CreationUpdatingUserJPC user = new AdminService.CreationUpdatingUserJPC();

            if(category != TValidationCategories.email)
                user.setEmail("test@gmail.com");

            switch (category){
                case email -> user.setEmail(value);
                case firstname -> user.setFirstname(value);
                case surname -> user.setSurname(value);
                case patronymic -> user.setPatronymic(value);
                case password -> user.setPassword(value);
                case role -> user.setRole(Roles.getById(Integer.parseInt(value)));
            }

            if(category != TValidationCategories.password)
                user.setPassword("password1234");


            if(category != TValidationCategories.role)
                user.setRole(Roles.ADMIN);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> adminService.createUser(gson.toJson(user)));

            assertEquals(ExceptionReason.VALIDATION_ERROR, expectedException.getReason());

            verify(usersDAO, never()).register(any(LimitedUser.class), anyString());
        }

        private static Stream<Arguments> provideInvalidUserParameters() {
            return Stream.of(
                    Arguments.of(
                            "maybe.email.com",
                            TValidationCategories.email
                    ),
                    Arguments.of(
                            "John12",
                            TValidationCategories.firstname
                    ),
                    Arguments.of(
                            "Fake2",
                            TValidationCategories.surname
                    ),
                    Arguments.of(
                            "Michael11",
                            TValidationCategories.patronymic
                    ),
                    Arguments.of(
                            "someBadPass",
                            TValidationCategories.password
                    ),
                    Arguments.of(
                            "0",
                            TValidationCategories.role
                    )
            );
        }
    }

    @Nested
    class getUser{
        @Test
        @DisplayName("User id correct")
        void getUserIdCorrect() throws DBException, DescriptiveException{
            LimitedUser user = TGenerators.genUser();
            int userId = usersDAO.insertUser(user);
            user.setId(userId);

            // Getting from service method
            InformativeUser foundUser = adminService.getUser(CryptoStore.encrypt(String.valueOf(userId)));

            assertNotNull(foundUser);
            assertEquals(userId, foundUser.getId());
            assertEquals(user.getEmail(), foundUser.getEmail());
        }

        @DisplayName("User id, which doesn`t exists")
        @Test
        void getUserIdIncorrect(){
            assertThrows(DescriptiveException.class, () -> adminService.getUser(CryptoStore.encrypt(String.valueOf(1))));
        }
    }

    @Nested
    class updateUser{

        @Test
        @DisplayName("Update successful")
        void updateSuccessful() throws Exception{
            LimitedUser originalUser = TGenerators.genUser();
            int originalUserId = usersDAO.insertUser(originalUser);
            originalUser.setId(originalUserId);

            AdminService.CreationUpdatingUserJPC user = new AdminService.CreationUpdatingUserJPC();

            user.setId(originalUserId);
            user.setEmail("someRandom@mail.ua");
            user.setFirstname("Mykhailo");
            user.setPatronymic("Dmytrovich");
            user.setRole(Roles.MANAGER);

            adminService.updateUser(gson.toJson(user));

            LimitedUser obtainedUser = usersDAO.get(originalUserId);

            assertThat(obtainedUser.getEmail()).isEqualTo(user.getEmail());
            assertThat(obtainedUser.getFirstname()).isEqualTo(user.getFirstname());
            assertThat(obtainedUser.getPatronymic()).isEqualTo(user.getPatronymic());
            assertThat(obtainedUser.getRole()).isEqualTo(user.getRole());
        }

        @ParameterizedTest
        @DisplayName("Failing validation")
        @MethodSource("provideInvalidUserParameters")
        void testUserDataFailValidation(String value, TValidationCategories category) throws Exception {
            AdminService.CreationUpdatingUserJPC user = new AdminService.CreationUpdatingUserJPC();

            // Setting up some random user id, because anyway it should be ignored
            user.setId(0);

            switch (category){
                case email -> user.setEmail(value);
                case firstname -> user.setFirstname(value);
                case surname -> user.setSurname(value);
                case patronymic -> user.setPatronymic(value);
                case password -> user.setPassword(value);
                case role -> user.setRole(Roles.getById(Integer.parseInt(value)));
            }

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> adminService.updateUser(gson.toJson(user)));

            assertEquals(ExceptionReason.VALIDATION_ERROR, expectedException.getReason());

            verify(usersDAO, never()).updateUsersData(anyInt(), any(HashMap.class));
        }

        private static Stream<Arguments> provideInvalidUserParameters() {
            return Stream.of(
                    Arguments.of(
                            "maybe.email.com",
                            TValidationCategories.email
                    ),
                    Arguments.of(
                            "John12",
                            TValidationCategories.firstname
                    ),
                    Arguments.of(
                            "Fake2",
                            TValidationCategories.surname
                    ),
                    Arguments.of(
                            "Michael11",
                            TValidationCategories.patronymic
                    ),
                    Arguments.of(
                            "someBadPass",
                            TValidationCategories.password
                    ),
                    Arguments.of(
                            "0",
                            TValidationCategories.role
                    )
            );
        }
    }


    @Test
    void updateUserState() throws Exception{
        LimitedUser user = TGenerators.genUser();
        int userId = usersDAO.insertUser(user);
        user.setId(userId);
        String encryptedUserId = CryptoStore.encrypt(String.valueOf(userId));

        // Incorrect case (state id doesn`t exist)
        assertThrows(NullPointerException.class, () -> adminService.updateUserState(encryptedUserId, 3));
        assertEquals(AccountStates.UNBLOCKED, usersDAO.get(userId).getState());

        // Correct case
        adminService.updateUserState(encryptedUserId, AccountStates.BLOCKED.id());
        assertEquals(AccountStates.BLOCKED, usersDAO.get(userId).getState());
    }

    @Test
    void deleteUsers() throws Exception{
        LimitedUser user1 = TGenerators.genUser();
        int user1Id = usersDAO.insertUser(user1);
        user1.setId(user1Id);

        LimitedUser user2 = TGenerators.genUser();
        int user2Id = usersDAO.insertUser(user2);
        user2.setId(user2Id);

        // Performing delete operation
        String encryptedUser1Id = CryptoStore.encrypt(String.valueOf(user1Id));
        String encryptedUser2Id = CryptoStore.encrypt(String.valueOf(user2Id));

        AdminService.DeleteUsersJPC deleteUsersJPC = new AdminService.DeleteUsersJPC(); // container for sending json data (deleteUsers method parses json with this class)

        deleteUsersJPC.setIds(List.of(encryptedUser1Id, encryptedUser2Id));

        adminService.deleteUsers(gson.toJson(deleteUsersJPC));

        assertEquals(0, usersDAO.getAll().size());
    }



    private enum TValidationCategories{email, firstname, surname, patronymic, password, role}
}