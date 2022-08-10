package com.github.DiachenkoMD.web.daos.impls.mysql;

import com.github.DiachenkoMD.entities.dto.invoices.Invoice;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MysqlInvoicesDAO implements InvoicesDAO {

    private static final Logger logger = LogManager.getLogger(MysqlInvoicesDAO.class);
    private final DataSource ds;

    public MysqlInvoicesDAO(DataSource ds){
        this.ds = ds;
    }

    @Override
    public HashMap<Invoice, String> getBasicConnectedWithCar(int carId) throws DBException{
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("CALL GetBasicInvoiceByCarID(?)");
        ){
            stmt.setInt(1, carId);

            HashMap<Invoice, String> foundInvoices = new HashMap<>();

            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    foundInvoices.put(Invoice.of(rs), rs.getString("email"));
                }
            }

            return foundInvoices;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public HashMap<Integer, String> getOnCar(int carId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT tbl_invoices.id, tbl_users.email FROM tbl_invoices\n" +
                        "JOIN tbl_users ON tbl_invoices.id = tbl_users.id\n" +
                        "WHERE tbl_invoices.car_id = ?");
        ){
            stmt.setInt(1, carId);

            HashMap<Integer, String> invoicesToClients = new HashMap<>();

            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    invoicesToClients.put(rs.getInt("id"), rs.getString("email"));
                }
            }

            return invoicesToClients;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }
}
