package com.github.DiachenkoMD.web.daos.prototypes;

import com.github.DiachenkoMD.entities.dto.drivers.ExtendedDriver;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.dto.users.InformativeUser;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.dto.users.PanelUser;
import com.github.DiachenkoMD.entities.exceptions.DBException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UsersDAO {
    /**
     * Method for getting user entity. {@link #get(int)} should be better used instead if possibly (in case speed matters).
     * @param email user email
     * @return {@link AuthUser} entity.
     * @throws DBException
     */
    AuthUser get(String email) throws DBException;

    /**
     * Method for getting user entity.
     * @param userId
     * @return {@link AuthUser} entity.
     * @throws DBException
     */
    AuthUser get(int userId) throws DBException;

    /**
     * Method for getting {@link LimitedUser user} from driver id.
     * @param driverId
     * @return user entity.
     */
    Optional<LimitedUser> getFromDriver(int driverId) throws DBException;

    /**
     * Method for gettin {@link ExtendedDriver driver} entity from user id.
     * @param userId
     * @return driver entity.
     */
    Optional<ExtendedDriver> getDriverFromUser(int userId) throws DBException;

    /**
     * Method for getting all available users. <br/>
     * Inside user entities, images are omitted.
     * @return list of found users.
     * @throws DBException
     */
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

    /**
     * Method for checking whether such user exists or not. Inside calls {@link #doesExist(String)}.
     * @param user user entity. Only emails is taken from it.
     * @return true if user exists and false otherwise.
     */
    boolean doesExist(LimitedUser user) throws DBException;

    /**
     * Method for checking whether such user exists or not.
     * @param email user`s email.
     * @return true if user exists and false otherwise.
     */
    boolean doesExist(String email) throws DBException;

    /**
     * Method for setting confirmation code for user. Can accept "null" value inside "confirmationCode" parameter.
     * @param userId
     * @param confirmationCode
     * @return true if more than one entity has been updated and false otherwise.
     */
    boolean setConfirmationCode(int userId, String confirmationCode) throws DBException;

    /**
     * Method for getting user by confirmation code. Might be used, for example, at email confirmation page.
     * @param code confirmation code.
     * @return found user entity.
     */
    AuthUser getUserByConfirmationCode(String code) throws DBException;

    /**
     * Method for updating user data.
     * @param userId
     * @param fieldsToUpdate can contain "firstname", "surname", "patronymic" etc. fields.
     * @implSpec fieldsToUpdated are references to db columns names.
     * @throws DBException
     */
    void updateUsersData(int userId, Map<String, String> fieldsToUpdate) throws DBException;

    /**
     * Method for getting user password.
     * @param userId
     * @return encrypted user password.
     * @throws DBException
     */
    String getPassword(int userId) throws DBException;

    /**
     * Method for setting user password.
     * @param userId
     * @param password new user password. Should better be encrypted.
     * @return true if more than one entry has been updated and false otherwise.
     */
    boolean setPassword(int userId, String password) throws DBException;

    /**
     * Method for getting user balance.
     * @param userId
     * @return double (should better made it BigDecimal) balance value.
     */
    double getBalance(int userId) throws DBException;

    /**
     * Method for setting user balance.
     * @param userId
     * @param newBalance
     * @return true if more than one entry has been updated and false otherwise.
     * @implNote inside calls {@link #setBalance(int, BigDecimal)}
     */
    boolean setBalance(int userId, double newBalance) throws DBException;

    /**
     * Method for setting user balance.
     * @param userId
     * @param newBalance
     * @return true if more than one entry has been updated and false otherwise.
     */
    boolean setBalance(int userId, BigDecimal newBalance) throws DBException;

    /**
     * Method for getting user avatar.
     * @param userId
     * @return user avatar.
     */
    Optional<String> getAvatar(int userId) throws DBException;

    /**
     * Method for setting user avatar.
     * @param userId
     * @param avatarName
     * @return
     * @throws DBException
     */
    boolean setAvatar(int userId, String avatarName) throws DBException;

    /**
     * Returns users which satisfied requested filters. Used with limit offset and limit count. <br/>
     * Field that are searched by (state on 2022.08.18):
     * <ul>
     *     <li>Email</li>
     *     <li>Firstname</li>
     *     <li>Surname</li>
     *     <li>Patronymic</li>
     *     <li>Role id</li>
     *     <li>Blocked status (blocked / unblocked)</li>
     * </ul>
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
     * @param userId id of the requested user.
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

    /**
     * Method for deleting users. Used at admin-panel.
     * @param userIds list of users` ids.
     * @return true if all entities were deleted and false otherwise.
     * @throws DBException
     */
    boolean deleteUsers(List<Integer> userIds) throws DBException;

    /**
     * Method for getting available drivers on a specified dates range. Logic of founding such in the db goes here the same as at {@link CarsDAO#getRentedDatesOnCar(int, LocalDate) there}.
     * @param start
     * @param end
     * @return drivers` ids
     * @throws DBException
     */
    List<Integer> getAvailableDriversOnRange(LocalDate start, LocalDate end, int cityId) throws DBException;

    /**
     * Method for setting city for a driver. Used at driver-panel to change dislocation place.
     * @param driverId
     * @param cityId city`s id from {@link com.github.DiachenkoMD.entities.enums.Cities Cities} enum.
     * @return true if more than one entry has been updated and false otherwise.
     * @throws DBException
     */
    boolean setDriverCity(int driverId, int cityId) throws DBException;


    // Methods for testing purposes

    /**
     * @param userId
     * @param cityId city`s id from {@link com.github.DiachenkoMD.entities.enums.Cities Cities} enum
     * @return id of newly created driver
     * @throws DBException
     */
    int insertDriver(int userId, int cityId) throws DBException;

    /**
     * @param user
     * @return id of newly created user
     * @throws DBException
     */
    int insertUser(LimitedUser user) throws DBException;
}
