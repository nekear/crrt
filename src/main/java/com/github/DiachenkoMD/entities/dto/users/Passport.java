package com.github.DiachenkoMD.entities.dto.users;

import com.github.DiachenkoMD.entities.enums.ValidationParameters;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.Validatable;
import com.google.gson.annotations.SerializedName;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

public class Passport {
    private String firstname;
    private String surname;
    private String patronymic;

    @SerializedName("date_of_birth")
    private LocalDate dateOfBirth;

    @SerializedName("date_of_issue")
    private LocalDate dateOfIssue;

    @SerializedName("doc_number")
    private long docNumber;

    private long rntrc;

    private Integer authority;

    public static Passport of(ResultSet rs) throws SQLException {
        Passport passport = new Passport();

        passport.setFirstname(rs.getString("pp_firstname"));
        passport.setSurname(rs.getString("pp_surname"));
        passport.setPatronymic(rs.getString("pp_patronymic"));
        passport.setDateOfBirth(rs.getDate("pp_date_of_birth").toLocalDate());
        passport.setDateOfIssue(rs.getDate("pp_date_of_issue").toLocalDate());
        passport.setDocNumber(rs.getLong("pp_doc_number"));
        passport.setRntrc(rs.getLong("pp_rntrc"));
        passport.setAuthority(rs.getInt("pp_authority"));

        return passport;
    }

    public void validate() throws DescriptiveException {
        Validatable firstnameVT = Validatable.of(firstname, ValidationParameters.NAME);
        Validatable surnameVT = Validatable.of(surname, ValidationParameters.NAME);
        Validatable patronymicVT = Validatable.of(patronymic, ValidationParameters.NAME);
        Validatable dateOfBirthVT = Validatable.of(dateOfBirth, ValidationParameters.DATE_OF_BIRTH);
        Validatable dateOfIssueVT = Validatable.of(dateOfIssue, ValidationParameters.DATE_OF_ISSUE);
        Validatable docNumberVT = Validatable.of(docNumber, ValidationParameters.DOC_NUMBER);
        Validatable rntrcVT = Validatable.of(rntrc, ValidationParameters.RNTRC);
        Validatable authorityVT = Validatable.of(authority, ValidationParameters.AUTHORITY);

        if(!Utils.validate(firstnameVT, surnameVT, patronymicVT, dateOfBirthVT, dateOfIssueVT, docNumberVT, rntrcVT, authorityVT))
            throw new DescriptiveException("VALIDATION ERROR: " + this, ExceptionReason.PASSPORT_VALIDATION_ERROR);
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

    public long getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(long docNumber) {
        this.docNumber = docNumber;
    }

    public long getRntrc() {
        return rntrc;
    }

    public void setRntrc(long rntrc) {
        this.rntrc = rntrc;
    }

    public int getAuthority() {
        return authority;
    }

    public void setAuthority(int authority) {
        this.authority = authority;
    }

    @Override
    public String toString() {
        return "Passport{" +
                "firstname='" + firstname + '\'' +
                ", surname='" + surname + '\'' +
                ", patronymic='" + patronymic + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", dateOfIssue=" + dateOfIssue +
                ", docNumber=" + docNumber +
                ", rntrc=" + rntrc +
                ", authority=" + authority +
                '}';
    }
}
