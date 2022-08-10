package com.github.DiachenkoMD.entities.dto;

import com.github.DiachenkoMD.entities.Constants;
import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.Transversal;
import com.github.DiachenkoMD.entities.enums.AccountStates;
import com.github.DiachenkoMD.entities.enums.Roles;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class User extends Transversal implements Serializable {
    protected Object id;
    protected String email;
    protected String firstname;
    protected String surname;
    protected String patronymic;
    protected Roles role;
    protected String avatar;
    protected double balance;
    protected String confirmationCode;
    protected AccountStates state;

    public User(Object id, String email, String firstname, String surname, String patronymic, Roles role, String avatar, double balance, String confirmationCode) {
        this.id = id;
        this.email = email;
        this.firstname = firstname;
        this.surname = surname;
        this.patronymic = patronymic;
        this.role = role;
        this.avatar = avatar;
        this.balance = balance;
        this.confirmationCode = confirmationCode;
    }

    public User(String email, String firstname, String surname, String patronymic) {
        this(null, email, firstname, surname, patronymic, Roles.CLIENT, null, 0, null);
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

    public String getAvatar() {
        return avatar;
    }
    public String idenAvatar(String prefix){
        if(this.avatar == null){
            return getUrlForNullAvatar();
        }else{
            return prefix + Constants.AVATAR_UPLOAD_DIR + "/" + avatar;
        }
    }

    public String getUrlForNullAvatar(){
        String customAvatarId = getLogin();
        return String.format("https://avatars.dicebear.com/api/identicon/%s.svg?background=333333", customAvatarId);
    }

    // This class doesn`t have login field in it and db neither, but this function allows to get prettified login from email
    public String getLogin(){
        return this.email == null ? "crr_user" : this.email.split("@")[0];
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public double getBalance() {
        return balance;
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

    public AccountStates getState() {
        return state;
    }

    public void setState(AccountStates state) {
        this.state = state;
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

    @Override
    public boolean encrypt() throws DescriptiveException {
        super.setObject(this.id);
        if(super.encrypt()){
            this.id = super.getObject();
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean decrypt() throws DescriptiveException {
        super.setObject(this.id);
        if(super.decrypt()){
            this.id = super.getObject();
            return true;
        }else{
            return false;
        }
    }

    @Override
    public Optional<Integer> getCleanId() throws DescriptiveException {
        super.setObject(this.id);
        return super.getCleanId();
    }

    public static User of(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getInt(DB_Constants.TBL_USERS_USER_ID),
                rs.getString(DB_Constants.TBL_USERS_EMAIL),
                rs.getString(DB_Constants.TBL_USERS_FIRSTNAME),
                rs.getString(DB_Constants.TBL_USERS_SURNAME),
                rs.getString(DB_Constants.TBL_USERS_PATRONYMIC),
                Roles.getById(rs.getInt(DB_Constants.TBL_USERS_ROLE_ID)),
                rs.getString(DB_Constants.TBL_USERS_AVATAR),
                rs.getDouble(DB_Constants.TBL_USERS_BALANCE),
                rs.getString(DB_Constants.TBL_USERS_CONF_CODE)
        );

        user.setState(AccountStates.getById(rs.getInt(DB_Constants.TBL_USERS_IS_BLOCKED)));

        return user;
    }
}
