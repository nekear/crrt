package com.github.DiachenkoMD.daos.prototypes;

import com.github.DiachenkoMD.dto.ExtendedUser;
import com.github.DiachenkoMD.dto.User;

import java.util.List;

public interface UsersDAO {
    ExtendedUser get(String email);
    List<User> getAll();
    <T extends User> List<T> getAll(Class<T> parseTo);
    User register(User user, String password);
    boolean doesExist(User user);

    boolean setConfirmationCode(String email, String confirmationCode);
    String generateConfirmationCode();

    ExtendedUser getUserByConfirmationCode(String code);
}
