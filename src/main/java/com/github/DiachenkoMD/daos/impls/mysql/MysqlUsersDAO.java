package com.github.DiachenkoMD.daos.impls.mysql;

import com.github.DiachenkoMD.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.dto.Roles;
import com.github.DiachenkoMD.dto.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MysqlUsersDAO implements UsersDAO {
    private final Connection con;

    public MysqlUsersDAO(Connection con){
        this.con = con;
    }

    @Override
    public User create(User user, String password) {
        System.out.println("Using: " + con);
        try(
                PreparedStatement stmt = con.prepareStatement("INSERT INTO tbl_users (email, password, username, surname, patronymic) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ){

            int index = 0;
            stmt.setString(++index, user.getEmail());
            stmt.setString(++index, password);
            stmt.setString(++index, user.getUsername());
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
            throw new RuntimeException(e);
        }
    }

    public List<User> getAll(){
        System.out.println("Using: " + con);
        try(
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_users");
            ResultSet rs = stmt.executeQuery();
        ){

            List<User> foundUsers = new ArrayList<>();

            while(rs.next()){
                foundUsers.add(
                        new User(
                              rs.getInt("id"),
                              rs.getString("email"),
                              rs.getString("username"),
                              rs.getString("surname"),
                              rs.getString("patronymic"),
                              Roles.byIndex(rs.getInt("role_id")),
                              rs.getString("avatar_path")
                        )
                );
            }

            return foundUsers;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
