package com.github.DiachenkoMD.tests.database;

import com.github.DiachenkoMD.daos.impls.mysql.MysqlUsersDAO;
import com.github.DiachenkoMD.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.dto.ExtendedUser;
import com.github.DiachenkoMD.dto.User;
import com.github.DiachenkoMD.extensions.ConnectionParameterResolverExtension;
import com.github.DiachenkoMD.extensions.DatabaseOperationsExtension;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.util.List;

@ExtendWith({
        DatabaseOperationsExtension.class,
        ConnectionParameterResolverExtension.class
})
public class UsersDAO_test {

    private UsersDAO usersDAO;

    public UsersDAO_test(Connection con){
        this.usersDAO = new MysqlUsersDAO(con);
    }

    @Nested
    @DisplayName("doesExist")
    class doesExistTests{
        @Test
        @DisplayName("Should find nothing")
        void testUserShouldntExist(){
            User testUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

            assertFalse(usersDAO.doesExist(testUser));
        }
        @Test
        @DisplayName("Should find one")
        void testUserShouldExist(){
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
        void testCreateNewUser(){
            User newUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

            assertInstanceOf(Integer.class, usersDAO.register(newUser, "1").getId());
        }

        @Test
        @DisplayName("Create new user without credentials")
        void testCreateNewUserWithoutCredentials(){
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
            assertNull(current.getUsername());
            assertNull(current.getSurname());
            assertNull(current.getPatronymic());
        }
    }

    @Test
    @DisplayName("get")
    void getUserTest(){
        User newUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

        usersDAO.register(newUser, "random_pass");

        ExtendedUser registered = usersDAO.get(newUser.getEmail());

        assertNotNull(registered.getId());
    }
    @Test
    @DisplayName("generateConfirmationCode")
    void generateUserCodeTest(){
        assertNotNull(usersDAO.generateConfirmationCode());
    }

    @Test
    @DisplayName("setConfirmationCode")
    void setConfirmationCodeTest(){
        User newUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

        usersDAO.register(newUser, "random_pass");

        ExtendedUser registered = usersDAO.get(newUser.getEmail());

        assertNull(registered.getConfirmationCode());

        String generatedCode = usersDAO.generateConfirmationCode();

        assertTrue(usersDAO.setConfirmationCode(registered.getEmail(), generatedCode));

        assertEquals(usersDAO.get(registered.getEmail()).getConfirmationCode(), generatedCode);
    }

    @Test
    @DisplayName("wipingConfirmationCode")
    void wipingConfirmationCodeTest(){
        User newUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

        usersDAO.register(newUser, "random_pass");

        ExtendedUser registered = usersDAO.get(newUser.getEmail());

        assertNull(registered.getConfirmationCode());

        String generatedCode = usersDAO.generateConfirmationCode();

        assertTrue(usersDAO.setConfirmationCode(registered.getEmail(), generatedCode));

        assertEquals(usersDAO.get(registered.getEmail()).getConfirmationCode(), generatedCode);

        assertTrue(usersDAO.setConfirmationCode(registered.getEmail(), null));

        assertNull(usersDAO.get(registered.getEmail()).getConfirmationCode());
    }
}
