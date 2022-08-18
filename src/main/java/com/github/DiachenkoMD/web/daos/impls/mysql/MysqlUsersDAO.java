package com.github.DiachenkoMD.web.daos.impls.mysql;

import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.dto.drivers.ExtendedDriver;
import com.github.DiachenkoMD.entities.dto.users.InformativeUser;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.dto.users.PanelUser;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.web.utils.Utils.generateRandomString;

public class MysqlUsersDAO implements UsersDAO {
    private static final Logger logger = LogManager.getLogger(MysqlUsersDAO.class);
    private final DataSource ds;

    public MysqlUsersDAO(DataSource ds){
        this.ds = ds;
    }

    @Override
    public AuthUser get(String email) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_users WHERE email=?");
        ){
            stmt.setString(1, email);
            AuthUser user = null;
            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    user = AuthUser.of(rs);
                }
            }
            return user;
        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public AuthUser get(int userId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_users WHERE id=?");
        ){
            stmt.setInt(1, userId);
            AuthUser user = null;
            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    user = AuthUser.of(rs);
                }
            }
            return user;
        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public Optional<LimitedUser> getFromDriver(int driverId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT tbl_users.id, tbl_users.firstname, tbl_users.surname, tbl_users.patronymic, tbl_users.email, tbl_users.role_id, tbl_users.is_blocked FROM tbl_drivers\n" +
                        "JOIN tbl_users ON tbl_drivers.user_id = tbl_users.id\n" +
                        "WHERE tbl_drivers.id = ?");
        ){
            stmt.setInt(1, driverId);

            LimitedUser driver = null;

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    driver = LimitedUser.of(rs);
                }
            }

            return Optional.ofNullable(driver);
        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public Optional<ExtendedDriver> getDriverFromUser(int userId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT tbl_drivers.id, tbl_drivers.city_id, tbl_users.email AS driver_email, tbl_users.avatar AS driver_avatar FROM tbl_drivers\n" +
                        "JOIN tbl_users ON tbl_drivers.user_id = tbl_users.id\n" +
                        "WHERE tbl_drivers.user_id = ?");
        ){
            stmt.setInt(1, userId);

            ExtendedDriver driver = null;

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    driver = ExtendedDriver.of(rs);
                }
            }

            return Optional.ofNullable(driver);
        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public List<AuthUser> getAll() throws DBException{
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_users");
                ResultSet rs = stmt.executeQuery();
        ){

            List<AuthUser> foundUsers = new ArrayList<>();

            while(rs.next()){
                foundUsers.add(AuthUser.of(rs));
            }

            return foundUsers;

        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    @Override
    public LimitedUser register(LimitedUser user, String password) throws DBException{
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("INSERT INTO tbl_users (email, password, firstname, surname, patronymic, role_id, is_blocked) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ){

            int index = 0;
            stmt.setString(++index, user.getEmail());
            stmt.setString(++index, password);
            stmt.setString(++index, user.getFirstname());
            stmt.setString(++index, user.getSurname());
            stmt.setString(++index, user.getPatronymic());
            stmt.setInt(++index, user.getRole().id());
            stmt.setInt(++index, user.getState().id());

            int affectedRows = stmt.executeUpdate();

            if(affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    Object new_id = rs.getInt(1);

                    user.setId(new_id);

                    return user;
                }
            }else{
                return null;
            }

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public AuthUser completeRegister(AuthUser user, String password) throws DBException{
        try(
            Connection con = ds.getConnection();
        ){
            // Generating new unique confirmation code
            String generated = null;
            boolean doesCodeExists = false;

            do{
                generated = generateRandomString(12);
                try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_users WHERE conf_code=?")){
                    stmt.setString(1, generated);
                    try(ResultSet rs = stmt.executeQuery()){
                        if(rs.next())
                            doesCodeExists = true;
                    }
                }
            }while(doesCodeExists);

            try(PreparedStatement stmt = con.prepareStatement("INSERT INTO tbl_users (email, password, firstname, surname, patronymic, conf_code) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)){
                int index = 0;
                stmt.setString(++index, user.getEmail());
                stmt.setString(++index, password);
                stmt.setString(++index, user.getFirstname());
                stmt.setString(++index, user.getSurname());
                stmt.setString(++index, user.getPatronymic());
                stmt.setString(++index, generated);

                int affectedRows = stmt.executeUpdate();

                if(affectedRows > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        rs.next();
                        Object new_id = rs.getInt(1);

                        user.setId(new_id);
                        user.setConfirmationCode(generated);

                        return user;
                    }
                }else{
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public boolean doesExist(LimitedUser user) throws DBException{
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT id FROM tbl_users WHERE email=?");
        ){

            stmt.setString(1, user.getEmail());

            try(ResultSet rs = stmt.executeQuery()){
                return rs.next();
            }
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }



    @Override
    public boolean setConfirmationCode(int userId, String confirmationCode) throws DBException{
        try(
            Connection con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE tbl_users SET conf_code=? WHERE id=?");
        ){
            stmt.setString(1, confirmationCode);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        }catch (SQLException e){
            throw new DBException(e);
        }
    }

    @Override
    public String generateConfirmationCode() throws DBException{
        String generated = null;

        boolean doesCodeExists = false;

        try(Connection con = ds.getConnection()){
            do{
                generated = generateRandomString(12);
                try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_users WHERE conf_code=?")){
                    stmt.setString(1, generated);
                    try(ResultSet rs = stmt.executeQuery()){
                        if(rs.next())
                            doesCodeExists = true;
                    }
                }
            }while(doesCodeExists);
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }

        return generated;
    }

    @Override
    public AuthUser getUserByConfirmationCode(String code) throws DBException{
        AuthUser user = null;

        try(
            Connection con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_users WHERE conf_code=?");
        ){
            stmt.setString(1, code);

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next())
                    user = AuthUser.of(rs);
            }

        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }

        return user;
    }

    @Override
    public boolean updateUsersData(int userId, HashMap<String, String> fieldsToUpdate) throws DBException {
        StringBuilder query = new StringBuilder("UPDATE tbl_users SET ");

        query.append(fieldsToUpdate.keySet().stream().map(s -> s + "=?").collect(Collectors.joining(", ")));

        query.append("WHERE " + DB_Constants.TBL_USERS_USER_ID + " = ?");
        try(
            Connection con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement(query.toString());
        ){
            int index = 0;
            for(String value : fieldsToUpdate.values()){
                stmt.setString(++index, value);
            }
            stmt.setInt(++index, userId);

            return stmt.executeUpdate() > 0;
        }catch (SQLException e){
            throw new DBException(e);
        }
    }

    @Override
    public String getPassword(int userId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT password FROM tbl_users WHERE id=?");
        ){
            stmt.setInt(1, userId);

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return rs.getString(DB_Constants.TBL_USERS_PASSWORD);
                }else{
                    return null;
                }
            }

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public boolean setPassword(int userId, String password) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("UPDATE tbl_users SET password=? WHERE id=?");
        ){

            stmt.setString(1, password);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public double getBalance(int userId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT balance FROM tbl_users WHERE id=?");
        ){
            stmt.setInt(1, userId);

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return rs.getDouble(DB_Constants.TBL_USERS_BALANCE);
                }else{
                    throw new DBException("Couldn`t get users balance. Target user id: " + userId);
                }
            }

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public boolean setBalance(int userId, double newBalance) throws DBException {
        this.setBalance(userId, BigDecimal.valueOf(newBalance));
        return true;
    }

    @Override
    public void setBalance(int userId, BigDecimal newBalance) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("UPDATE tbl_users SET balance=? WHERE id=?");
        ){

            stmt.setBigDecimal(1, newBalance);
            stmt.setInt(2, userId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public Optional<String> getAvatar(int user_id) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT avatar FROM tbl_users WHERE id=?");
        ){
            stmt.setInt(1, user_id);

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return Optional.ofNullable(rs.getString(DB_Constants.TBL_USERS_AVATAR));
                }else{
                    throw new DBException("Couldn`t get user avatar. Target user id: " + user_id);
                }
            }

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public boolean setAvatar(int user_id, String avatarName) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("UPDATE tbl_users SET avatar=? WHERE id=?");
        ){

            stmt.setString(1, avatarName);
            stmt.setInt(2, user_id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public List<PanelUser> getUsersWithFilters(HashMap<String, String> filters, int limitOffset, int limitCount) throws DBException {
        if(filters.size() == 0)
            filters.put("id", "%");

        String query = "SELECT id, email, firstname, surname, patronymic, role_id, is_blocked FROM tbl_users WHERE "
                + filters.keySet().stream().map(s -> s + " LIKE ?").collect(Collectors.joining(" AND "))
                + " LIMIT " + limitOffset + ", " + limitCount;

        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement(query);
        ){

            int index = 0;
            for(String value : filters.values()){
                stmt.setString(++index, value);
            }

            List<PanelUser> foundUsers = new ArrayList<>();

            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    foundUsers.add(PanelUser.of(rs));
                }
            }

            return foundUsers;

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public int getUsersNumberWithFilters(HashMap<String, String> filters) throws DBException {
        if(filters.size() == 0)
            filters.put("id", "%");

        String query = "SELECT COUNT(id) AS counted FROM tbl_users WHERE "
                + filters.keySet().stream().map(s -> s + " LIKE ?").collect(Collectors.joining(" AND "));

        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement(query);
        ){

            int index = 0;
            for(String value : filters.values()){
                stmt.setString(++index, value);
            }


            try(ResultSet rs = stmt.executeQuery()){
                rs.next();

                return rs.getInt("counted");

            }

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public InformativeUser getInformativeUser(int userId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement(
                        "SELECT id, email,firstname,surname,patronymic,role_id,is_blocked,balance, conf_code, ts_created, " +
                                "(SELECT COUNT(id) FROM tbl_invoices WHERE tbl_invoices.client_id = tbl_users.id) AS invoicesAmount, " +
                                "getLastInvoiceCity(tbl_users.id) AS lastInvoiceCity " +
                                "FROM tbl_users WHERE id=?");
        ){
            stmt.setInt(1, userId);
            InformativeUser user = null;
            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    user = InformativeUser.of(rs);
                }
            }

            return user;
        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public boolean setUserState(int userId, int stateId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("UPDATE tbl_users SET is_blocked=? WHERE id=?");
        ){

            stmt.setInt(1, stateId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public boolean deleteUsers(List<Integer> userIds) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("DELETE FROM tbl_users WHERE id=?");
        ){

            for(Integer userId : userIds){
                stmt.setInt(1, userId);
                stmt.addBatch();
            }

            return stmt.executeBatch().length == userIds.size();

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }


    @Override
    public List<Integer> getAvailableDriversOnRange(LocalDate start, LocalDate end, int cityId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT id FROM tbl_drivers WHERE (SELECT COUNT(id) FROM tbl_invoices WHERE date_start <= ? AND date_end >= ? AND tbl_invoices.driver_id = tbl_drivers.id) = 0 AND tbl_drivers.city_id = ?");
        ){

            stmt.setObject(1, end);
            stmt.setObject(2, start);
            stmt.setInt(3, cityId);

            List<Integer> foundAvailableDrivers = new LinkedList<>();
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next())
                    foundAvailableDrivers.add(rs.getInt("id"));
            }

            return foundAvailableDrivers;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public boolean setDriverCity(int driverId, int cityId) throws DBException {
        try(
            Connection con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE tbl_drivers SET city_id = ? WHERE id = ?");
        ){

            stmt.setInt(1, cityId);
            stmt.setInt(2, driverId);

            return stmt.executeUpdate() > 0;

        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    // Methods for testing purposes

    @Override
    public int insertDriver(int userId, int cityId) throws DBException {
        try(
            Connection con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement("INSERT INTO tbl_drivers (user_id, city_id) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)
        ){
            stmt.setInt(1, userId);
            stmt.setInt(2, cityId);

            int newlyInsertedDriverId;

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                newlyInsertedDriverId = rs.getInt(1);
            }

            return newlyInsertedDriverId;
        }catch(SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public int insertUser(LimitedUser user) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("INSERT INTO tbl_users (email, password, firstname, surname, patronymic, role_id, is_blocked) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ){

            int index = 0;
            stmt.setString(++index, user.getEmail());
            stmt.setString(++index, "createdForTest");
            stmt.setString(++index, user.getFirstname());
            stmt.setString(++index, user.getSurname());
            stmt.setString(++index, user.getPatronymic());
            stmt.setInt(++index, user.getRole().id());
            stmt.setInt(++index, user.getState().id());

            int affectedRows = stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }
}
