package com.github.DiachenkoMD.entities.dto.users;

import com.google.gson.annotations.SerializedName;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Passport {
    private String firstname;
    private String surname;
    private String patronymic;

    @SerializedName("date_of_birth")
    private LocalDate dateOfBirth;

    @SerializedName("date_of_issue")
    private LocalDate dateOfIssue;

    @SerializedName("doc_number")
    private int docNumber;

    private int rntrc;

    private int authority;

    public static Passport of(ResultSet rs) throws SQLException {
        Passport passport = new Passport();

        passport.setFirstname(rs.getString("pp_firstname"));
        passport.setSurname(rs.getString("pp_surname"));
        passport.setPatronymic(rs.getString("pp_patronymic"));
        passport.setDateOfBirth(rs.getDate("pp_date_of_birth").toLocalDate());
        passport.setDateOfIssue(rs.getDate("pp_date_of_issue").toLocalDate());
        passport.setDocNumber(rs.getInt("pp_doc_number"));
        passport.setRntrc(rs.getInt("pp_rntrc"));
        passport.setAuthority(rs.getInt("pp_authority"));

        return passport;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getDateOfIssue() {
        return dateOfIssue;
    }

    public void setDateOfIssue(LocalDate dateOfIssue) {
        this.dateOfIssue = dateOfIssue;
    }

    public int getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(int docNumber) {
        this.docNumber = docNumber;
    }

    public int getRntrc() {
        return rntrc;
    }

    public void setRntrc(int rntrc) {
        this.rntrc = rntrc;
    }

    public int getAuthority() {
        return authority;
    }

    public void setAuthority(int authority) {
        this.authority = authority;
    }
}
