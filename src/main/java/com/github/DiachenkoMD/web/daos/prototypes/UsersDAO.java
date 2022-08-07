package com.github.DiachenkoMD.web.daos.prototypes;

import com.github.DiachenkoMD.entities.dto.User;
import com.github.DiachenkoMD.entities.exceptions.DBException;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface UsersDAO {
    User get(String email) throws DBException;

    List<User> getAll() throws DBException;
    User register(User user, String password) throws DBException;
    User completeRegister(User user, String password) throws DBException;
    boolean doesExist(User user) throws DBException;

    boolean setConfirmationCode(int user_id, String confirmationCode) throws DBException;
    String generateConfirmationCode() throws DBException;

    User getUserByConfirmationCode(String code) throws DBException;

    boolean updateUsersData(int user_id, HashMap<String, String> fields_to_update) throws DBException;
    String getPassword(int user_id) throws DBException;
    boolean setPassword(int user_id, String password) throws DBException;

    double getBalance(int user_id) throws DBException;
    boolean setBalance(int user_id, double newBalance) throws DBException;

    Optional<String> getAvatar(int id) throws DBException;
    boolean setAvatar(int id, String avatarName) throws DBException;

}
