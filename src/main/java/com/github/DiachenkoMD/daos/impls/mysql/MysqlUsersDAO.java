package com.github.DiachenkoMD.daos.impls.mysql;

import com.github.DiachenkoMD.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.dto.Roles;
import com.github.DiachenkoMD.dto.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MysqlUsersDAO implements UsersDAO {
    private final Connection con;

    public MysqlUsersDAO(Connection con){
        this.con = con;
    }

    @Override
    public boolean addUser() {
        return false;
    }

    public List<User> getAll(){
        System.out.println("Using: " + con);
        try(
            con;
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
