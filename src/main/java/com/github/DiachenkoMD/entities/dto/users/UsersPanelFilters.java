package com.github.DiachenkoMD.entities.dto.users;

import com.github.DiachenkoMD.entities.adapters.DBCoupledAdapter;
import com.github.DiachenkoMD.entities.dto.Filters;
import com.github.DiachenkoMD.entities.enums.AccountStates;
import static com.github.DiachenkoMD.entities.DB_Constants.*;
import static com.github.DiachenkoMD.web.utils.Utils.clean;

import com.github.DiachenkoMD.entities.enums.DBCoupled;
import com.github.DiachenkoMD.entities.enums.Roles;
import com.google.gson.annotations.JsonAdapter;

import java.util.HashMap;

public class UsersPanelFilters extends Filters {
    private String email;
    private String firstname;
    private String surname;
    private String patronymic;
    @JsonAdapter(DBCoupledAdapter.class)
    private Roles role;
    @JsonAdapter(DBCoupledAdapter.class)
    private AccountStates state;

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


    public HashMap<String, String> getDBPresentation(String part) {
        HashMap<String, String> representation = new HashMap<>();

        if(email != null && !email.isBlank())
            representation.put(TBL_USERS_EMAIL, dgp(part, clean(email)));
        if(firstname != null && !firstname.isBlank())
            representation.put(TBL_USERS_FIRSTNAME,  dgp(part, clean(firstname)));
        if(surname != null && !surname.isBlank())
            representation.put(TBL_USERS_SURNAME,  dgp(part, clean(surname)));
        if(patronymic != null && !patronymic.isBlank())
            representation.put(TBL_USERS_PATRONYMIC,  dgp(part, clean(patronymic)));
        if(role != null && role.id() > 0){
            representation.put(TBL_USERS_ROLE_ID, String.valueOf(role.id()));
        }
        if(state != null && state.id() > 0){
            representation.put(TBL_USERS_IS_BLOCKED, String.valueOf(state.id()));
        }

        return representation;
    }

    @Override
    public HashMap<String, String> getDBPresentation() {
        return this.getDBPresentation("");
    }


    @Override
    public String toString() {
        return String.format("{\n" +
                "   Email: %s,\n" +
                "   Firstname: %s,\n" +
                "   Surname: %s,\n" +
                "   Patronymic: %s,\n" +
                "   Role: %s,\n" +
                "   State: %s\n" +
                "}", this.getEmail(), this.getFirstname(), this.getSurname(), this.getPatronymic(), this.getRole(), this.getState());
    }

    private String dgp(String glued, String str){
        return glued + str + glued;
    }
}
