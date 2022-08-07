package com.github.DiachenkoMD.tests.database;

import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlUsersDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.entities.dto.User;
import com.github.DiachenkoMD.extensions.ConnectionParameterResolverExtension;
import com.github.DiachenkoMD.extensions.DatabaseOperationsExtension;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@ExtendWith({
        DatabaseOperationsExtension.class,
        ConnectionParameterResolverExtension.class
})
class UsersDAOTest {

    private final UsersDAO usersDAO;

    public UsersDAOTest(DataSource ds){
        this.usersDAO = new MysqlUsersDAO(ds);
    }

    @Nested
    @DisplayName("doesExist")
    class doesExistTests{
        @Test
        @DisplayName("Should find nothing")
        void testUserShouldntExist() throws Exception {
            User testUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

            assertFalse(usersDAO.doesExist(testUser));
        }
        @Test
        @DisplayName("Should find one")
        void testUserShouldExist() throws Exception {
            User testUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

            usersDAO.register(testUser, "1");

            assertTrue(usersDAO.doesExist(testUser));
        }
    }

    @Nested
    @DisplayName("register")
    class registerTests{
        @Test
        @DisplayName("Create new user")
        void testCreateNewUser() throws Exception {
            User newUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

            assertInstanceOf(Integer.class, usersDAO.register(newUser, "1").getId());
        }

        @Test
        @DisplayName("Create new user without credentials")
        void testCreateNewUserWithoutCredentials() throws Exception {
            User newUser = new User("test@gmail.com", null, null, null);

            Integer id = (Integer) usersDAO.register(newUser, "password1").getId();

            User foundUser = usersDAO.get(newUser.getEmail());


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
            User newUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

            User created = usersDAO.completeRegister(newUser, "password1");

            assertDoesNotThrow(() -> created.getCleanId().get());

            assertNotNull(created.getConfirmationCode());
        }

        @Test
        @DisplayName("Create new user without credentials")
        void testCreateNewUserWithoutCredentials() throws Exception {
            User newUser = new User("test@gmail.com", null, null, null);

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

    @Test
    @DisplayName("get")
    void getUserTest() throws Exception {
        User fakeUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

        usersDAO.completeRegister(fakeUser, "random_pass");

        User acquiredUser = usersDAO.get(fakeUser.getEmail());

        assertNotNull(fakeUser.getId());
        assertEquals(fakeUser.getFirstname(), acquiredUser.getFirstname());
        assertEquals(fakeUser.getSurname(), acquiredUser.getSurname());
        assertEquals(fakeUser.getPatronymic(), acquiredUser.getPatronymic());
        assertNotNull(fakeUser.getConfirmationCode()); // thats value is generated so we have to set it

        assertNotNull(acquiredUser.getConfirmationCode());
    }

    @Test
    @DisplayName("getAll")
    void getAllUsersTest() throws Exception {
        assertEquals(usersDAO.getAll().size(), 0);

        User newUser = new User("test@gmail.com", "Firstname", "Surname", "Patronymic");
        User newUser2 = new User("test2@gmail.com", "Firstname", "Surname", "Patronymic");

        usersDAO.register(newUser, "random_pass");
        usersDAO.register(newUser2, "random_pass");

        assertEquals(2, usersDAO.getAll().size());
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
        User registered = usersDAO.completeRegister(new User("email@gmail.com", null, null, null), "password1");

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
    @DisplayName("Overall set tests")
    void overallSetTests() throws Exception{
        // Registering new user entity
        User registered = usersDAO.register(new User("email@gmail.com", null, null, null), "password1");

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
    }

    @Test
    @DisplayName("updateUsersData")
    void updateUsersDataTest() throws Exception{
        // Registering new user entity
        User registered = usersDAO.register(new User("email@gmail.com", null, null, null), "password1");

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
        User acquiredFromDBUser = usersDAO.get(registered.getEmail());

        assertEquals(firstname, acquiredFromDBUser.getFirstname());
        assertEquals(surname, acquiredFromDBUser.getSurname());
        assertEquals(patronymic, acquiredFromDBUser.getPatronymic());
    }
}
