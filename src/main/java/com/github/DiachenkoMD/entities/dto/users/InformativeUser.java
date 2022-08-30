package com.github.DiachenkoMD.entities.dto.users;

import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.adapters.DBCoupledAdapter;
import com.github.DiachenkoMD.entities.enums.AccountStates;
import com.github.DiachenkoMD.entities.enums.Cities;
import com.github.DiachenkoMD.entities.enums.Roles;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Designed for storing extended information about specific user. Might be used, for example, at admin-panel when getting info about specific users.
 */
public class InformativeUser extends LimitedUser{
    private double balance;
    private String confirmationCode;
    private int invoicesAmount;


    private Cities city; // will be based on last invoice
    @SerializedName("regDate")
    private LocalDateTime tsCreated;

    public static InformativeUser of(ResultSet rs) throws SQLException {
        InformativeUser user = new InformativeUser();

        user.setId(rs.getInt(DB_Constants.TBL_USERS_USER_ID));
        user.setEmail(rs.getString(DB_Constants.TBL_USERS_EMAIL));
        user.setFirstname(rs.getString(DB_Constants.TBL_USERS_FIRSTNAME));
        user.setSurname(rs.getString(DB_Constants.TBL_USERS_SURNAME));
        user.setPatronymic(rs.getString(DB_Constants.TBL_USERS_PATRONYMIC));
        user.setRole(Roles.getById(rs.getInt(DB_Constants.TBL_USERS_ROLE_ID)));
        user.setState(AccountStates.getById(rs.getInt(DB_Constants.TBL_USERS_IS_BLOCKED)));
        user.setBalance(rs.getDouble(DB_Constants.TBL_USERS_BALANCE));
        user.setConfirmationCode(rs.getString(DB_Constants.TBL_USERS_CONF_CODE));
        user.setInvoicesAmount(rs.getInt("invoicesAmount"));
        user.setTsCreated(rs.getTimestamp("ts_created").toLocalDateTime());

        if(user.invoicesAmount > 0)
            user.setCity(Cities.getById(rs.getInt("lastInvoiceCity")));

        return user;
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

    public int getInvoicesAmount() {
        return invoicesAmount;
    }

    public void setInvoicesAmount(int invoicesAmount) {
        this.invoicesAmount = invoicesAmount;
    }

    public LocalDateTime getTsCreated() {
        return tsCreated;
    }

    public void setTsCreated(LocalDateTime tsCreated) {
        this.tsCreated = tsCreated;
    }

    public Cities getCity() {
        return city;
    }

    public void setCity(Cities city) {
        this.city = city;
    }
}
