package com.github.DiachenkoMD.dto;

public class ExtendedUser extends User{

    protected double balance;

    protected String confirmationCode;

    public ExtendedUser(int id, String email, String username, String surname, String patronymic, Roles role_id, String avatar_path) {
        super(id, email, username, surname, patronymic, role_id, avatar_path);
    }

    public ExtendedUser(int id, String email, String username, String surname, String patronymic, Roles role_id, String avatar_path, double balance, String confirmationCode) {
        this(id, email, username, surname, patronymic, role_id, avatar_path);
        this.balance = balance;
        this.confirmationCode = confirmationCode;
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
}
