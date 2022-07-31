package com.github.DiachenkoMD.daos.prototypes;

import com.github.DiachenkoMD.dto.User;

import java.util.List;

public interface UsersDAO {
    List<User> getAll();
    User create(User user, String password);
}
