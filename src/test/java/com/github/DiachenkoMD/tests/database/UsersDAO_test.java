package com.github.DiachenkoMD.tests.database;

import com.github.DiachenkoMD.daos.impls.mysql.MysqlUsersDAO;
import com.github.DiachenkoMD.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.dto.User;
import com.github.DiachenkoMD.extensions.ConnectionParameterResolverExtension;
import com.github.DiachenkoMD.extensions.DatabaseOperationsExtension;
import com.github.DiachenkoMD.extensions.ExecutionContextExtension;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
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

    @Test
    @DisplayName("Get empty users list")
    void testGetEmptyUsersList(){
        List<User> foundUsers = usersDAO.getAll();

        assertEquals(foundUsers.size(), 0);
    }

    @Test
    @DisplayName("Create new user")
    void testCreateNewUser(){
        User newUser = new User("test@gmail.com", "Mykhailo", "Diachenko", "Dmytrovich");

        assertInstanceOf(Integer.class, usersDAO.create(newUser, "1").getId());
    }

    @Test
    @DisplayName("Create new user without credentials")
    void testCreateNewUserWithoutCredentials(){
        User newUser = new User("test@gmail.com", null, null, null);

        Integer id = (Integer) usersDAO.create(newUser, "1").getId();

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
