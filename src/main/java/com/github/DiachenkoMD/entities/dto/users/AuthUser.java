package com.github.DiachenkoMD.entities.dto.users;

import com.github.DiachenkoMD.entities.Constants;
import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.enums.AccountStates;
import com.github.DiachenkoMD.entities.enums.Roles;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthUser extends LimitedUser implements Serializable {
    protected String avatar;
    protected double balance;
    protected String confirmationCode;

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

    @Override
    public String toString() {
        return "AuthUser{" +
                "avatar='" + avatar + '\'' +
                ", balance=" + balance +
                ", confirmationCode='" + confirmationCode + '\'' +
                "} " + super.toString();
    }

    // TODO:: add comparing encoded id (String) to decoded id (Integer)
    @Override
    public boolean equals(final Object obj) {
        if(obj == null)
            return false;

        if(!(obj instanceof AuthUser))
            return false;

        AuthUser incoming = (AuthUser) obj;
        if(this.id != null && incoming.getId() != null)
            return this.id.equals(incoming.getId());

        if(this.email != null && incoming.getEmail() != null)
            return this.email.equalsIgnoreCase(incoming.getEmail());

        return true;
    }

    public static AuthUser of(ResultSet rs) throws SQLException {
        AuthUser user = new AuthUser();

        user.setId(rs.getInt(DB_Constants.TBL_USERS_USER_ID));
        user.setEmail(rs.getString(DB_Constants.TBL_USERS_EMAIL));
        user.setFirstname(rs.getString(DB_Constants.TBL_USERS_FIRSTNAME));
        user.setSurname(rs.getString(DB_Constants.TBL_USERS_SURNAME));
        user.setPatronymic(rs.getString(DB_Constants.TBL_USERS_PATRONYMIC));
        user.setRole(Roles.getById(rs.getInt(DB_Constants.TBL_USERS_ROLE_ID)));
        user.setAvatar(rs.getString(DB_Constants.TBL_USERS_AVATAR));
        user.setBalance(rs.getDouble(DB_Constants.TBL_USERS_BALANCE));
        user.setConfirmationCode(rs.getString(DB_Constants.TBL_USERS_CONF_CODE));
        user.setState(AccountStates.getById(rs.getInt(DB_Constants.TBL_USERS_IS_BLOCKED)));

        return user;
    }

    // These two methods were created mainly for backward compatibility, because originally I developed AuthUser (User originally) without nesting and with chaotic constructor of 8 or 9 parameters.
    public static AuthUser of(String email, String firstname, String surname, String patronymic){
        AuthUser user = new AuthUser();
        user.setEmail(email);
        user.setFirstname(firstname);
        user.setSurname(surname);
        user.setPatronymic(patronymic);
        return user;
    }

    public static AuthUser of(Object id, String email, String firstname, String surname, String patronymic, Roles role, String avatar, double balance, String confirmationCode){
        AuthUser user = AuthUser.of(email, firstname, surname, patronymic);
        user.setId(id);
        user.setRole(role);
        user.setAvatar(avatar);
        user.setBalance(balance);
        user.setConfirmationCode(confirmationCode);
        return user;
    }


}
