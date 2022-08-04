package com.github.DiachenkoMD.dto;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    protected Object id;
    protected String email;
    protected String firstname;
    protected String surname;
    protected String patronymic;
    protected Roles role;
    protected String avatarPath;
    protected double balance;

    protected String confirmationCode;
    protected String password;

    public User(Object id, String email, String firstname, String surname, String patronymic, Roles role, String avatarPath, double balance, String confirmationCode, String password) {
        this.id = id;
        this.email = email;
        this.firstname = firstname;
        this.surname = surname;
        this.patronymic = patronymic;
        this.role = role;
        this.avatarPath = avatarPath;
        this.balance = balance;
        this.confirmationCode = confirmationCode;
        this.password = password;
    }

    public User(String email, String firstname, String surname, String patronymic) {
        this(null, email, firstname, surname, patronymic, Roles.DEFAULT, null, 0, null, null);
    }

    public User(){} // for reflective parser and testing

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

    public void setFirstname(String username) {
        this.firstname = username;
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

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public double getBalance() {
        return balance;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    @Override
    public String toString() {
        return this.id + " -> " + this.email;
    }

    // TODO:: add comparing encoded id (String) to decoded id (Integer)
    @Override
    public boolean equals(final Object obj) {
        if(obj == null)
            return false;

        if(!(obj instanceof User))
            return false;

        User incoming = (User) obj;
        if(this.id != null && incoming.getId() != null)
            return this.id.equals(incoming.getId());

        if(this.email != null && incoming.getEmail() != null)
            return this.email.equalsIgnoreCase(incoming.getEmail());

        return true;
    }

    public static User getFromRS(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("firstname"),
                rs.getString("surname"),
                rs.getString("patronymic"),
                Roles.byIndex(rs.getInt("role_id")),
                rs.getString("avatar_path"),
                rs.getFloat("balance"),
                rs.getString("conf_code"),
                rs.getString("password")
        );
    }
}
