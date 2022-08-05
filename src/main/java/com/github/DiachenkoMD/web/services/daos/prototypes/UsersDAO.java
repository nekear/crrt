package com.github.DiachenkoMD.web.services.daos.prototypes;

import com.github.DiachenkoMD.entities.dto.User;
import com.github.DiachenkoMD.entities.exceptions.DBException;

import java.sql.SQLException;
import java.util.List;

public interface UsersDAO {
    User get(String email) throws DBException;
    List<User> getAll();
    User register(User user, String password);
    boolean doesExist(User user) throws DBException;

    boolean setConfirmationCode(String email, String confirmationCode);
    String generateConfirmationCode() throws DBException;

    User getUserByConfirmationCode(String code);
}
