package com.github.DiachenkoMD.dto;

public class User {
    private Object id;
    private String email;
    private String username;
    private String surname;
    private String patronymic;
    private Roles role;

    private String avatarPath;

    public User(Object id, String email, String username, String surname, String patronymic, Roles role, String avatarPath) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.surname = surname;
        this.patronymic = patronymic;
        this.role = role;
        this.avatarPath = avatarPath;
    }

    public User(String email, String username, String surname, String patronymic) {
        this(null, email, username, surname, patronymic, Roles.DEFAULT, null);
    }

    public User(){} // for reflective parser

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    @Override
    public String toString() {
        return this.id + " -> " + this.email;
    }

    // TODO:: add comparing encoded id (String) to decoded id (Integer)
    @Override
    public boolean equals(final Object obj) {
        if(obj == null)
            return false;

        if(!(obj instanceof User))
            return false;

        User incoming = (User) obj;
        if(this.id != null && incoming.getId() != null)
            return this.id.equals(incoming.getId());

        if(this.email != null && incoming.getEmail() != null)
            return this.email.equalsIgnoreCase(incoming.getEmail());

        return true;
    }

}
