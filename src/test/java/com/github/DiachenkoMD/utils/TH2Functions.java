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

    public static int getActiveRepairsByInvoiceId(Connection con, int invoiceId) throws SQLException{
        try(
                PreparedStatement stmt = con.prepareStatement("SELECT COUNT(id) AS counted \n" +
                        "\tFROM tbl_repair_invoices WHERE expiration_date >= CURDATE() AND is_paid = 0 AND invoice_id = ?;");
        ){
            stmt.setInt(1, invoiceId);

            try(ResultSet rs = stmt.executeQuery()){
                rs.next();

                return rs.getInt("counted");
            }
        }
    }

    public static int getExpiredRepairsByInvoiceId(Connection con, int invoiceId) throws SQLException{
        try(
                PreparedStatement stmt = con.prepareStatement("SELECT COUNT(id) AS counted \n" +
                        "\tFROM tbl_repair_invoices WHERE expiration_date < CURDATE() AND is_paid = 0 AND invoice_id = ?;");
        ){
            stmt.setInt(1, invoiceId);

            try(ResultSet rs = stmt.executeQuery()){
                rs.next();

                return rs.getInt("counted");
            }
        }
    }

    public static Integer getLastInvoiceCity(Connection con, int clientId) throws SQLException{
        try(
                PreparedStatement stmt = con.prepareStatement("SELECT tbl_cars.city_id AS city FROM tbl_invoices\n" +
                        "\tJOIN tbl_cars ON tbl_invoices.car_id = tbl_cars.id\n" +
                        "\tWHERE tbl_invoices.client_id = ?\n" +
                        "\tORDER BY tbl_invoices.ts_created\n" +
                        "\tLIMIT 1;");
        ){
            stmt.setInt(1, clientId);

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next())
                    return rs.getInt("city");

                return null;
            }
        }
    }
}
