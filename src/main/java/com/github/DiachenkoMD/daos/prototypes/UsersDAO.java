package com.github.DiachenkoMD.daos.prototypes;

import com.github.DiachenkoMD.dto.User;

import java.util.List;

public interface UsersDAO {
    User get(String email);
    List<User> getAll();
    User register(User user, String password);
    boolean doesExist(User user);

    boolean setConfirmationCode(String email, String confirmationCode);
    String generateConfirmationCode();

    User getUserByConfirmationCode(String code);
}
