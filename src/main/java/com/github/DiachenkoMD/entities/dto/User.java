package com.github.DiachenkoMD.entities.dto;

import com.github.DiachenkoMD.entities.Constants;
import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.web.utils.CryptoStore;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class User implements Serializable {
    protected Object id;
    protected String email;
    protected String firstname;
    protected String surname;
    protected String patronymic;
    protected Roles role;
    protected String avatar;
    protected double balance;
    protected String confirmationCode;

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
        this(null, email, firstname, surname, patronymic, Roles.DEFAULT, null, 0, null);
    }

    public User(){} // for reflective parser and testing

    public Object getId() {
        return id;
    }

    public Optional<Integer> getCleanId() throws DescriptiveException{
        if(this.id == null)
            return Optional.empty();

        if(this.id instanceof String encryptedId)
            return Optional.of(Integer.valueOf(CryptoStore.decrypt(encryptedId)));

        return Optional.of((Integer) this.id);
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
        String customAvatarId;
        if(this.email != null) {
            customAvatarId = this.email.split("@")[0];
        }else{
            customAvatarId = "crrt_user";
        }
        return String.format("https://avatars.dicebear.com/api/identicon/%s.svg?background=333333", customAvatarId);
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
                rs.getInt(DB_Constants.TBL_USERS_USER_ID),
                rs.getString(DB_Constants.TBL_USERS_EMAIL),
                rs.getString(DB_Constants.TBL_USERS_FIRSTNAME),
                rs.getString(DB_Constants.TBL_USERS_SURNAME),
                rs.getString(DB_Constants.TBL_USERS_PATRONYMIC),
                Roles.byIndex(rs.getInt(DB_Constants.TBL_USERS_ROLE_ID)),
                rs.getString(DB_Constants.TBL_USERS_AVATAR),
                rs.getDouble(DB_Constants.TBL_USERS_BALANCE),
                rs.getString(DB_Constants.TBL_USERS_CONF_CODE)
        );
    }

    public void decrypt() throws DescriptiveException {
        if(this.id instanceof String idEncrypted)
            this.id = Integer.valueOf(CryptoStore.decrypt(idEncrypted));
    }

    public void encrypt() throws DescriptiveException{
        if(this.id instanceof Integer idDecrypted)
            this.id = CryptoStore.encrypt(String.valueOf(idDecrypted));
    }
}
