package com.github.DiachenkoMD.web.daos.impls.mysql;

import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.dto.LimitedUser;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.entities.dto.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
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
    public User get(String email) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_users WHERE email=?");
        ){
            stmt.setString(1, email);
            User user = null;
            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    user = User.of(rs);
                }
            }
            return user;
        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }
    public List<User> getAll() throws DBException{
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_users");
                ResultSet rs = stmt.executeQuery();
        ){

            List<User> foundUsers = new ArrayList<>();

            while(rs.next()){
                foundUsers.add(User.of(rs));
            }

            return foundUsers;

        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    @Override
    public User register(User user, String password) throws DBException{
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("INSERT INTO tbl_users (email, password, firstname, surname, patronymic) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ){

            int index = 0;
            stmt.setString(++index, user.getEmail());
            stmt.setString(++index, password);
            stmt.setString(++index, user.getFirstname());
            stmt.setString(++index, user.getSurname());
            stmt.setString(++index, user.getPatronymic());

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
    public User completeRegister(User user, String password) throws DBException{
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
    public boolean doesExist(User user) throws DBException{
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
    public boolean setConfirmationCode(int user_id, String confirmationCode) throws DBException{
        try(
            Connection con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE tbl_users SET conf_code=? WHERE id=?");
        ){
            stmt.setString(1, confirmationCode);
            stmt.setInt(2, user_id);

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
    public User getUserByConfirmationCode(String code) throws DBException{
        User user = null;

        try(
            Connection con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_users WHERE conf_code=?");
        ){
            stmt.setString(1, code);

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next())
                    user = User.of(rs);
            }

        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }

        return user;
    }

    @Override
    public boolean updateUsersData(int user_id, HashMap<String, String> fields_to_update) throws DBException {
        StringBuilder query = new StringBuilder("UPDATE tbl_users SET ");

        query.append(fields_to_update.keySet().stream().map(s -> s + "=?").collect(Collectors.joining(", ")));

        query.append("WHERE " + DB_Constants.TBL_USERS_USER_ID + " = ?");
        try(
            Connection con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement(query.toString());
        ){
            int index = 0;
            for(String value : fields_to_update.values()){
                stmt.setString(++index, value);
            }
            stmt.setInt(++index, user_id);

            return stmt.executeUpdate() > 0;
        }catch (SQLException e){
            throw new DBException(e);
        }
    }

    @Override
    public String getPassword(int user_id) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT password FROM tbl_users WHERE id=?");
        ){
            stmt.setInt(1, user_id);

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
    public boolean setPassword(int user_id, String password) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("UPDATE tbl_users SET password=? WHERE id=?");
        ){

            stmt.setString(1, password);
            stmt.setInt(2, user_id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public double getBalance(int user_id) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT balance FROM tbl_users WHERE id=?");
        ){
            stmt.setInt(1, user_id);

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return rs.getDouble(DB_Constants.TBL_USERS_BALANCE);
                }else{
                    throw new DBException("Couldn`t get users balance. Target user id: " + user_id);
                }
            }

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public boolean setBalance(int user_id, double newBalance) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("UPDATE tbl_users SET balance=? WHERE id=?");
        ){

            stmt.setDouble(1, newBalance);
            stmt.setInt(2, user_id);

            return stmt.executeUpdate() > 0;

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
    public List<LimitedUser> getUserWithFilters(HashMap<String, String> filters, int limitStart, int limitEnd) throws DBException {

        String query = "SELECT id, email, firstname, surname, patronymic, role_id, is_blocked FROM tbl_users WHERE"
                + filters.keySet().stream().map(s -> s + " LIKE ?").collect(Collectors.joining(", "))
                + " LIMIT " + limitStart + ", " + limitEnd;
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement(query);
        ){

            int index = 0;
            for(String value : filters.values()){
                stmt.setString(++index, value);
            }

            List<LimitedUser> foundUsers = new ArrayList<>();

            try(ResultSet rs = stmt.getResultSet()){
                while(rs.next()){
                    foundUsers.add(LimitedUser.of(rs));
                }
            }

            return foundUsers;

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }
}
