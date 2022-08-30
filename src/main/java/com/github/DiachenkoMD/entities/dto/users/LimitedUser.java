package com.github.DiachenkoMD.entities.dto.users;

import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.adapters.CryptoAdapter;
import com.github.DiachenkoMD.entities.adapters.DBCoupledAdapter;
import com.github.DiachenkoMD.entities.enums.AccountStates;
import com.github.DiachenkoMD.entities.enums.Roles;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.google.gson.annotations.JsonAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Basic user entity. Contains little info about user. Not used at page and acts more like parent for more informative classes.
 */
public class LimitedUser {
    @JsonAdapter(CryptoAdapter.class)
    protected Object id;
    protected String email;
    protected String firstname;
    protected String surname;
    protected String patronymic;


    protected Roles role = Roles.CLIENT;

    protected AccountStates state = AccountStates.UNBLOCKED;


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

    public boolean isBlocked(){
        return state.equals(AccountStates.BLOCKED);
    }

    public boolean encrypt() throws DescriptiveException {
        if(this.id instanceof Integer decryptedId){
            this.id = CryptoStore.encrypt(String.valueOf(decryptedId));
            return true;
        }

        return false;
    }

    public boolean decrypt() throws DescriptiveException {
        if(this.id instanceof String encryptedId) {
            this.id = CryptoStore.decrypt(encryptedId);
            return true;
        }

        return false;
    }

    public Optional<Integer> getCleanId() throws DescriptiveException {
        if(this.id == null)
            return Optional.empty();

        if(this.id instanceof String encryptedId)
            return Optional.of(Integer.valueOf(CryptoStore.decrypt(encryptedId)));

        return Optional.of((Integer) this.id);
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

    @Override
    public String toString() {
        String id = null;
        try {
            id = String.valueOf(getCleanId().orElse(null));
        } catch (DescriptiveException ignored) {
            System.out.println("LIMITED_USER toString() exception");
        }

        return String.format("LIMITED_USER:{%s, %s, %s, %s, %s, %s, %s}", id, email, firstname, surname, patronymic, role, state);
    }

}
