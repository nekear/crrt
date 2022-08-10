package com.github.DiachenkoMD.entities.dto;

import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.enums.AccountStates;
import com.github.DiachenkoMD.entities.enums.Roles;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LimitedUser {
    private Object id;
    private String email;
    private String firstname;
    private String surname;
    private String patronymic;
    private Roles role;
    private AccountStates state;


    public static LimitedUser of(ResultSet rs) throws SQLException {
        LimitedUser user = new LimitedUser();

        user.setId(rs.getInt(DB_Constants.TBL_USERS_USER_ID));
        user.setEmail(rs.getString(DB_Constants.TBL_USERS_EMAIL));
        user.setFirstname(rs.getString(DB_Constants.TBL_USERS_FIRSTNAME));
        user.setSurname(rs.getString(DB_Constants.TBL_USERS_SURNAME));
        user.setPatronymic(rs.getString(DB_Constants.TBL_USERS_PATRONYMIC));
        user.setRole(Roles.getById(rs.getInt(DB_Constants.TBL_USERS_ROLE_ID)));
        user.setState(AccountStates.getById(rs.getInt(DB_Constants.TBL_USERS_IS_BLOCKED)));

        return user;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public AccountStates getState() {
        return state;
    }

    public void setState(AccountStates state) {
        this.state = state;
    }
}
