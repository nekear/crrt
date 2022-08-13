package com.github.DiachenkoMD.entities.dto.drivers;

import com.github.DiachenkoMD.entities.adapters.Skip;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LimitedDriver {
    private String avatar;
    private String code;

    @Skip
    private String email; // should not be exposed to client side

    protected LimitedDriver(ResultSet rs) throws SQLException {
        this.avatar = rs.getString("driver_avatar");
        this.code = rs.getString("driver_code");
        this.code = rs.getString("driver_email");
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
