package com.github.DiachenkoMD.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 */
public class TH2Functions {
    public static String glueCarPhotos(Connection con, int carId) throws SQLException {
        try(
            PreparedStatement stmt = con.prepareStatement("SELECT GROUP_CONCAT(CONCAT(id, '#', photo) SEPARATOR '?')\n" +
                    "  FROM tbl_cars_photos\n" +
                    "  WHERE car_id = ? GROUP BY car_id");
        ){
            stmt.setInt(1, carId);

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next())
                    return rs.getString(1);
            }

            return null;
        }
    }
}
