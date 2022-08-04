package com.github.DiachenkoMD.daos.impls.mysql;

import com.github.DiachenkoMD.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.dto.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.github.DiachenkoMD.utils.Utils.generateRandomString;

public class MysqlUsersDAO implements UsersDAO {
    private static final Logger logger = LogManager.getLogger(MysqlUsersDAO.class);
    private final Connection con;

    public MysqlUsersDAO(Connection con){
        this.con = con;
    }


    @Override
    public User register(User user, String password) {
        logger.debug("Using: " + con);
        try(
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
                    Integer new_id = rs.getInt(1);

                    user.setId(new_id);

                    return user;
                }
            }else{
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public List<User> getAll(){
       logger.debug("Using: " + con);
        try(
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_users");
            ResultSet rs = stmt.executeQuery();
        ){

            List<User> foundUsers = new ArrayList<>();

            while(rs.next()){
                foundUsers.add(User.getFromRS(rs));
            }

            return foundUsers;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean doesExist(User user) {
        logger.debug("Using: " + con);
        try(
                PreparedStatement stmt = con.prepareStatement("SELECT id FROM tbl_users WHERE email=?");
        ){

            stmt.setString(1, user.getEmail());

            try(ResultSet rs = stmt.executeQuery()){
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User get(String email) {
        try(
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_users WHERE email=?");
        ){
            stmt.setString(1, email);
            User user = null;
            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    user = User.getFromRS(rs);
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String generateConfirmationCode() {
        String generated;
        boolean doesCodeExists = false;

        do{
            generated = generateRandomString(12);

            try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_users WHERE conf_code=?")){
                stmt.setString(1, generated);
                try(ResultSet rs = stmt.executeQuery()){
                    if(rs.next())
                        doesCodeExists = true;
                }
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
        }while(doesCodeExists);

        return generated;
    }

    @Override
    public boolean setConfirmationCode(String email, String confirmationCode) {
        try(PreparedStatement stmt = con.prepareStatement("UPDATE tbl_users SET conf_code=? WHERE email=?")){
            stmt.setString(1, confirmationCode);
            stmt.setString(2, email);

            return stmt.executeUpdate() > 0;
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public User getUserByConfirmationCode(String code) {
        User user = null;

        try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_users WHERE conf_code=?")){
            stmt.setString(1, code);

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next())
                    user = User.getFromRS(rs);
            }

        }catch (SQLException e){
            logger.error(e);
        }

        return user;
    }
}
