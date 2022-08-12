package com.github.DiachenkoMD.entities.dto.drivers;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LimitedDriver {
    private String avatar;
    private String code;

    protected LimitedDriver(ResultSet rs) throws SQLException {
        this.avatar = rs.getString("driver_avatar");
        this.code = rs.getString("driver_code");
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
}
