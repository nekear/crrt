package com.github.DiachenkoMD.dto;

/**
 * Extended user class containing balance, confirmationCode and password. Entities of this class should not be exposed to client because they may contain sensitive data! <br>
 */
public class ExtendedUser extends User{

    protected double balance;

    protected String confirmationCode;
    protected String password;

    public ExtendedUser(int id, String email, String username, String surname, String patronymic, Roles role_id, String avatar_path) {
        super(id, email, username, surname, patronymic, role_id, avatar_path);
    }

    public ExtendedUser(int id, String email, String username, String surname, String patronymic, Roles role_id, String avatar_path, double balance, String confirmationCode, String password) {
        this(id, email, username, surname, patronymic, role_id, avatar_path);
        this.balance = balance;
        this.confirmationCode = confirmationCode;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
