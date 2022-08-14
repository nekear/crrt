package com.github.DiachenkoMD.web.daos.prototypes;

import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.dto.users.InformativeUser;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.dto.users.PanelUser;
import com.github.DiachenkoMD.entities.exceptions.DBException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface UsersDAO {
    AuthUser get(String email) throws DBException;

    AuthUser get(int userId) throws DBException;

    List<AuthUser> getAll() throws DBException;

    /**
     * (1/2) First registration method. (Second is {@link #completeRegister(AuthUser, String) completeRegister(AuthUser, String)}. Simply registers user and returns its instance setting newly generated id to it. <br/>
     * The second "sibling" method provides more life-needed functionality. This method doesn`t generate any confirmation code nor set it, for such functionality checkout {@link #completeRegister(AuthUser, String) comleteRegister}.
     * @param user from that`s user method will take all necessary information to register new user.
     * @param password encrypted (I hope) password of the registering user.
     * @return The same instance of user, that was passed to that method, but it adds newly generated id to it.
     * @throws DBException
     */
    LimitedUser register(LimitedUser user, String password) throws DBException;

    /**
     * (2/2) Second registration method. (First is {@link #register(LimitedUser, String) register(LimitedUser, String)}). Generates unique confirmation code, register user and returns it (filled with id and confirmation). <br/>
     * This method is, basically, extension for register method, which doesn`t generate confirmation code and is used at admin-panel user creation where we don`t have any need to set confirmation code to user.
     * @param user from that`s user method will take all necessary information to register new user.
     * @param password encrypted (I hope) password of the registering user.
     * @return The same instance of user, that was passed to that method, but it adds newly generated id and confirmation code to it.
     * @throws DBException
     */
    AuthUser completeRegister(AuthUser user, String password) throws DBException;
    boolean doesExist(LimitedUser user) throws DBException;

    boolean setConfirmationCode(int userId, String confirmationCode) throws DBException;
    String generateConfirmationCode() throws DBException;

    AuthUser getUserByConfirmationCode(String code) throws DBException;

    boolean updateUsersData(int userId, HashMap<String, String> fieldsToUpdate) throws DBException;
    String getPassword(int userId) throws DBException;
    boolean setPassword(int userId, String password) throws DBException;

    double getBalance(int userId) throws DBException;
    boolean setBalance(int userId, double newBalance) throws DBException;
    void setBalance(int userId, BigDecimal newBalance) throws DBException;

    Optional<String> getAvatar(int id) throws DBException;
    boolean setAvatar(int id, String avatarName) throws DBException;

    /**
     * Returns users which satisfied requested filters. Used with limit offset and limit count.
     * @param filters HashMap, where key - name of a column in db and value - corresponding value to filter by.
     * @param limitOffset from where to start selecting users (starts with 0)
     * @param limitCount how many users should be selected
     * @return List of {@link PanelUser PanelUser} entities. They represent (simply put, limited) user entities with some basic information used for fast search at admin-panel.
     * @throws DBException
     */
    List<PanelUser> getUsersWithFilters(HashMap<String, String> filters, int limitOffset, int limitCount) throws DBException;

    /**
     * Used to create pagination. Provides amount of users that could be returned if no limit was set.
     * @param filters HashMap, where key - name of a column in db and value - corresponding value to filter by.
     * @return amount of users that could be returned if not limit was set.
     * @throws DBException
     */
    int getUsersNumberWithFilters(HashMap<String, String> filters) throws DBException;

    /**
     * Returns extended info about the requested user. Used at admin-panel in user-editing section.
     * @param userId id of the requested user
     * @return {@link InformativeUser InformativeUser} with all fields filled.
     * @throws DBException
     */
    InformativeUser getInformativeUser(int userId) throws DBException;

    /**
     * Method for updating user state blocking / unblocking user.
     * @param userId
     * @param stateId
     * @return
     * @throws DBException
     */
    boolean setUserState(int userId, int stateId) throws DBException;

    boolean deleteUsers(List<Integer> userIds) throws DBException;
}
