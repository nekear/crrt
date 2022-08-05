package com.github.DiachenkoMD.tests.database;

import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.web.services.daos.impls.mysql.MysqlUsersDAO;
import com.github.DiachenkoMD.web.services.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.entities.dto.User;
import com.github.DiachenkoMD.extensions.ConnectionParameterResolverExtension;
import com.github.DiachenkoMD.extensions.DatabaseOperationsExtension;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
import java.util.List;

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
        void testUserShouldntExist() throws DBException {
            User testUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

            assertFalse(usersDAO.doesExist(testUser));
        }
        @Test
        @DisplayName("Should find one")
        void testUserShouldExist() throws DBException {
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
        void testCreateNewUser() throws DBException {
            User newUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

            assertInstanceOf(Integer.class, usersDAO.register(newUser, "1").getId());
        }

        @Test
        @DisplayName("Create new user without credentials")
        void testCreateNewUserWithoutCredentials() throws DBException {
            User newUser = new User("test@gmail.com", null, null, null);

            Integer id = (Integer) usersDAO.register(newUser, "1").getId();

            List<User> foundUsers = usersDAO.getAll();

            User current = null;

            for(User user : foundUsers){
                if(user.getId() == id){
                    current = user;
                    break;
                }
            }

            // Checking whether insertion was correct
            assertNotNull(current);

            // Checking for not having credentials set
            assertNull(current.getFirstname());
            assertNull(current.getSurname());
            assertNull(current.getPatronymic());
        }
    }

    @Test
    @DisplayName("get")
    void getUserTest() throws DBException {
        User newUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

        usersDAO.register(newUser, "random_pass");

        User registered = usersDAO.get(newUser.getEmail());

        assertNotNull(registered.getId());
        assertNotNull(registered.getFirstname());
        assertNotNull(registered.getSurname());
        assertNotNull(registered.getPatronymic());
    }

    @Test
    @DisplayName("getAll")
    void getAllUsersTest() throws DBException {
        assertEquals(usersDAO.getAll().size(), 0);

        User newUser = new User("test@gmail.com", "Firstname", "Surname", "Patronymic");
        User newUser2 = new User("test2@gmail.com", "Firstname", "Surname", "Patronymic");

        usersDAO.register(newUser, "random_pass");
        usersDAO.register(newUser2, "random_pass");

        assertEquals(2, usersDAO.getAll().size());
    }
    @Test
    @DisplayName("generateConfirmationCode")
    void generateUserCodeTest() throws DBException {
        assertNotNull(usersDAO.generateConfirmationCode());
    }


    @Test
    @DisplayName("setConfirmationCode")
    void setConfirmationCodeTest() throws DBException {
        User newUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

        usersDAO.register(newUser, "random_pass");

        User registered = usersDAO.get(newUser.getEmail());

        assertNull(registered.getConfirmationCode());

        // Setting some random code
        String generatedCode = usersDAO.generateConfirmationCode();

        assertTrue(usersDAO.setConfirmationCode(registered.getEmail(), generatedCode));

        assertEquals(usersDAO.get(registered.getEmail()).getConfirmationCode(), generatedCode);

        // Wiping code (setting null)
        assertTrue(usersDAO.setConfirmationCode(registered.getEmail(), null));

        assertNull(usersDAO.get(registered.getEmail()).getConfirmationCode());
    }

}
