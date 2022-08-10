package com.github.DiachenkoMD.entities.dto;

import com.github.DiachenkoMD.entities.adapters.DBCoupledAdapter;
import com.github.DiachenkoMD.entities.enums.AccountStates;
import static com.github.DiachenkoMD.entities.DB_Constants.*;
import com.github.DiachenkoMD.entities.enums.DBCoupled;
import com.github.DiachenkoMD.entities.enums.Roles;
import com.google.gson.annotations.JsonAdapter;

import java.util.HashMap;

public class UsersPanelFilters extends Filters{
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

    @Override
    public HashMap<String, String> getDBPresentation() {
        HashMap<String, String> representation = new HashMap<>();

        if(email != null)
            representation.put(TBL_USERS_EMAIL, email.trim());
        if(firstname != null)
            representation.put(TBL_USERS_FIRSTNAME, firstname.trim());
        if(surname != null)
            representation.put(TBL_USERS_SURNAME, surname.trim());
        if(patronymic != null)
            representation.put(TBL_USERS_PATRONYMIC, patronymic.trim());
        if(role != null){
            String role_id = role.id() > 0 ? String.valueOf(role.id()) : String.valueOf(Roles.CLIENT.id());
            representation.put(TBL_USERS_ROLE_ID, role_id);
        }
        if(state != null){
            String state_id = state.id() > 0 ? String.valueOf(state.id()) : String.valueOf(AccountStates.UNBLOCKED.id());
            representation.put(TBL_USERS_IS_BLOCKED, state_id);
        }

        return representation;
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
}
