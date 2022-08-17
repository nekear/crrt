package com.github.DiachenkoMD.entities.dto.drivers;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LimitedDriver {
    private String avatar;
    private String email;

    public LimitedDriver(){};
    protected LimitedDriver(ResultSet rs) throws SQLException {
        this.avatar = rs.getString("driver_avatar");
        this.email = rs.getString("driver_email");
    }

    public static LimitedDriver of(ResultSet rs) throws SQLException{
        return new LimitedDriver(rs);
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
