package com.github.DiachenkoMD.web.daos.impls.mysql;

import static com.github.DiachenkoMD.entities.DB_Constants.*;
import com.github.DiachenkoMD.entities.dto.invoices.LimitedInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.PanelInvoice;
import com.github.DiachenkoMD.entities.dto.users.PanelUser;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.web.utils.Utils.multieq;

public class MysqlInvoicesDAO implements InvoicesDAO {

    private static final Logger logger = LogManager.getLogger(MysqlInvoicesDAO.class);
    private final DataSource ds;

    public MysqlInvoicesDAO(DataSource ds){
        this.ds = ds;
    }

    @Override
    public HashMap<LimitedInvoice, String> getBasicConnectedWithCar(int carId) throws DBException{
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("CALL GetBasicInvoiceByCarID(?)");
        ){
            stmt.setInt(1, carId);

            HashMap<LimitedInvoice, String> foundInvoices = new HashMap<>();

            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    foundInvoices.put(LimitedInvoice.of(rs), rs.getString("email"));
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

    @Override
    public List<PanelInvoice> getPanelInvoicesWithFilters(HashMap<String, String> filters, int limitOffset, int limitCount) throws DBException{
        String query = "SELECT tbl_invoices.id AS invoice_id, tbl_invoices.code AS invoice_code,\n" +
                "       tbl_invoices.date_start, tbl_invoices.date_end,\n" +
                "       tbl_invoices.exp_price, tbl_invoices.is_canceled, tbl_invoices.is_rejected,\n" +
                "       getActiveRepairsByInvoiceId(tbl_invoices.id) AS activeRepairs,\n" +
                "       getExpiredRepairsByInvoiceId(tbl_invoices.id) AS expiredRepairs,\n" +
                "       tbl_invoices.driver_id, tbl_drivers.code AS driver_code, driver_u.avatar AS driver_avatar,\n" +
                "       client_u.email AS client_email,\n" +
                "       tbl_cars.brand, tbl_cars.model\n" +
                "FROM tbl_invoices\n" +
                "         LEFT JOIN tbl_drivers ON tbl_invoices.driver_id = tbl_drivers.id\n" +
                "         LEFT JOIN tbl_users AS driver_u ON tbl_drivers.user_id = driver_u.id\n" +
                "         JOIN tbl_users AS client_u ON tbl_invoices.client_id = client_u.id\n" +
                "         JOIN tbl_cars ON tbl_invoices.car_id = tbl_cars.id ";

        StringJoiner queryJoiner = new StringJoiner(" AND ", "WHERE ", " ");

        // Concatenating simple fields that will be searched by LIKE
        if(filters.size() > 0){
            String likes = filters.entrySet()
                    .parallelStream()
                    .filter(x -> !multieq(x.getKey(), "carName", TBL_INVOICES_DATE_START, TBL_INVOICES_DATE_END))
                    .map(x -> x.getKey() + " LIKE ?" )
                    .collect(Collectors.joining(" AND "));

            if(!likes.isBlank())
                queryJoiner.add(likes);
        }


        // Special "name" fields, which contains Brand + Model combination should be searched by MATCH AGAINST
        if(filters.containsKey("carName"))
            queryJoiner.add("MATCH(tbl_cars.brand, tbl_cars.model) AGAINST (? IN BOOLEAN MODE)");

        // Concatenation for Date format
        if(filters.containsKey(TBL_INVOICES_DATE_START))
            queryJoiner.add(TBL_INVOICES_DATE_START + " >= " + filters.get(TBL_INVOICES_DATE_START));

        if(filters.containsKey(TBL_INVOICES_DATE_END))
            queryJoiner.add(TBL_INVOICES_DATE_END + " <= " + filters.get(TBL_INVOICES_DATE_END));

        queryJoiner.setEmptyValue("");

        query += queryJoiner.toString();

        query += "ORDER BY tbl_invoices.ts_created LIMIT " + limitOffset + ", " + limitCount;

        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement(query);
        ){

            int index = 0;
            for(Map.Entry<String, String> ent : filters.entrySet()){
                logger.info("Setting {} to {}", ent.getKey(), ent.getValue());
                stmt.setString(++index, ent.getValue());
            }

            System.out.println(query);

            List<PanelInvoice> foundInvoices = new LinkedList<>();

            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    foundInvoices.add(PanelInvoice.of(rs));
                }
            }

            logger.info("GONNA RETURN {}", foundInvoices);

            return foundInvoices;

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public int getPanelInvoicesNumberWithFilters(HashMap<String, String> filters) throws DBException {
        String query = "SELECT COUNT(tbl_invoices.id) AS counted\n" +
                "FROM tbl_invoices" +
                "         LEFT JOIN tbl_drivers ON tbl_invoices.driver_id = tbl_drivers.id\n" +
                "         LEFT JOIN tbl_users AS driver_u ON tbl_drivers.user_id = driver_u.id\n" +
                "         JOIN tbl_users AS client_u ON tbl_invoices.client_id = client_u.id\n" +
                "         JOIN tbl_cars ON tbl_invoices.car_id = tbl_cars.id ";

        StringJoiner queryJoiner = new StringJoiner(" AND ", "WHERE ", " ");

        // Concatenating simple fields that will be searched by LIKE
        if(filters.size() > 0){
            String likes = filters.entrySet()
                    .parallelStream()
                    .filter(x -> !multieq(x.getKey(), "carName", TBL_INVOICES_DATE_START, TBL_INVOICES_DATE_END))
                    .map(x -> x.getKey() + " LIKE ?" )
                    .collect(Collectors.joining(" AND "));

            if(!likes.isBlank())
                queryJoiner.add(likes);
        }

        // Special "name" fields, which contains Brand + Model combination should be searched by MATCH AGAINST
        if(filters.containsKey("carName"))
            queryJoiner.add("MATCH(tbl_cars.brand, tbl_cars.model) AGAINST (? IN BOOLEAN MODE)");

        // Concatenation for Date format
        if(filters.containsKey(TBL_INVOICES_DATE_START))
            queryJoiner.add(TBL_INVOICES_DATE_START + " >= " + filters.get(TBL_INVOICES_DATE_START));

        if(filters.containsKey(TBL_INVOICES_DATE_END))
            queryJoiner.add(TBL_INVOICES_DATE_END + " <= " + filters.get(TBL_INVOICES_DATE_END));

        queryJoiner.setEmptyValue("");

        query += queryJoiner.toString();

        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement(query);
        ){

            int index = 0;
            for(String value : filters.values()){
                stmt.setString(++index, value);
            }

            try(ResultSet rs = stmt.executeQuery()){
               rs.next();

               return rs.getInt("counted");
            }

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }
}
