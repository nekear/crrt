package com.github.DiachenkoMD.entities.dto.users;

import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.enums.AccountStates;
import com.github.DiachenkoMD.entities.enums.Roles;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class, designed to store little information about invoice to pretty display it on the admin-panel table.
 */
public class PanelUser extends LimitedUser{

    public static PanelUser of(ResultSet rs) throws SQLException {
        PanelUser user = new PanelUser();

        user.setId(rs.getInt(DB_Constants.TBL_USERS_USER_ID));
        user.setEmail(rs.getString(DB_Constants.TBL_USERS_EMAIL));
        user.setFirstname(rs.getString(DB_Constants.TBL_USERS_FIRSTNAME));
        user.setSurname(rs.getString(DB_Constants.TBL_USERS_SURNAME));
        user.setPatronymic(rs.getString(DB_Constants.TBL_USERS_PATRONYMIC));
        user.setRole(Roles.getById(rs.getInt(DB_Constants.TBL_USERS_ROLE_ID)));
        user.setState(AccountStates.getById(rs.getInt(DB_Constants.TBL_USERS_IS_BLOCKED)));

        return user;
    }
}
