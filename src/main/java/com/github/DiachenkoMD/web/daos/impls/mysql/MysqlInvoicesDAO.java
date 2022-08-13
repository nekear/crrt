package com.github.DiachenkoMD.web.daos.impls.mysql;

import static com.github.DiachenkoMD.entities.DB_Constants.*;

import com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.LimitedInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.PanelInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.RepairInvoice;
import com.github.DiachenkoMD.entities.dto.users.PanelUser;
import com.github.DiachenkoMD.entities.enums.InvoiceStatuses;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
                "       tbl_invoices.driver_id, tbl_drivers.code AS driver_code, driver_u.avatar AS driver_avatar,driver_u.email AS driver_email,\n" +
                "       client_u.email AS client_email,\n" +
                "       tbl_cars.brand, tbl_cars.model\n" +
                "FROM tbl_invoices\n" +
                "         LEFT JOIN tbl_drivers ON tbl_invoices.driver_id = tbl_drivers.id\n" +
                "         LEFT JOIN tbl_users AS driver_u ON tbl_drivers.user_id = driver_u.id\n" +
                "         JOIN tbl_users AS client_u ON tbl_invoices.client_id = client_u.id\n" +
                "         JOIN tbl_cars ON tbl_invoices.car_id = tbl_cars.id ";

        // Concatenating simple fields that will be searched by LIKE
        String conditions = filters.keySet()
                .stream()
                .map(key -> {
                    switch (key) {
                        // Special "name" fields, which contains Brand + Model combination should be searched by MATCH AGAINST
                        case "carName":
                            return "MATCH(tbl_cars.brand, tbl_cars.model) AGAINST (? IN BOOLEAN MODE)";
                        // Concatenation for Date format
                        case TBL_INVOICES_DATE_START:
                            return TBL_INVOICES_DATE_START + " >= ?";
                        case TBL_INVOICES_DATE_END:
                            return TBL_INVOICES_DATE_END + " <= ?";
                        // For different statuses
                        case "tbl_invoices."+TBL_INVOICES_IS_CANCELED:
                            return "tbl_invoices."+TBL_INVOICES_IS_CANCELED + " = ?";
                        case "tbl_invoices."+TBL_INVOICES_IS_REJECTED:
                            return "tbl_invoices."+TBL_INVOICES_IS_REJECTED + " = ?";
                        case "activeRepairs":
                            return "getActiveRepairsByInvoiceId(tbl_invoices.id) > ?";
                        case "expiredRepairs":
                            return "getExpiredRepairsByInvoiceId(tbl_invoices.id) > ?";
                        // Concatenation for all other default fields
                        default:
                            if (filters.get(key) == null) {
                                return key + " IS ?";
                            } else {
                                return key + " LIKE ?";
                            }
                    }
                })
                .collect(Collectors.joining(" AND "));

        if(!conditions.isBlank()) {
            query += "WHERE ";
            query += conditions;
        }

        query += " ORDER BY tbl_invoices.ts_created DESC LIMIT " + limitOffset + ", " + limitCount;

        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement(query);
        ){

            int index = 0;
            for(String value : filters.values()){
                stmt.setString(++index, value);
            }

            List<PanelInvoice> foundInvoices = new LinkedList<>();

            try(ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    foundInvoices.add(PanelInvoice.of(rs));
                }
            }

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

        // Concatenating simple fields that will be searched by LIKE
        String conditions = filters.keySet()
                .stream()
                .map(key -> {
                    switch (key) {
                        // Special "name" fields, which contains Brand + Model combination should be searched by MATCH AGAINST
                        case "carName":
                            return "MATCH(tbl_cars.brand, tbl_cars.model) AGAINST (? IN BOOLEAN MODE)";
                        // Concatenation for Date format
                        case TBL_INVOICES_DATE_START:
                            return TBL_INVOICES_DATE_START + " >= ?";
                        case TBL_INVOICES_DATE_END:
                            return TBL_INVOICES_DATE_END + " <= ?";
                        // For different statuses
                        case "tbl_invoices."+TBL_INVOICES_IS_CANCELED:
                            return "tbl_invoices."+TBL_INVOICES_IS_CANCELED + " = ?";
                        case "tbl_invoices."+TBL_INVOICES_IS_REJECTED:
                            return "tbl_invoices."+TBL_INVOICES_IS_REJECTED + " = ?";
                        case "activeRepairs":
                            return "getActiveRepairsByInvoiceId(tbl_invoices.id) > ?";
                        case "expiredRepairs":
                            return "getExpiredRepairsByInvoiceId(tbl_invoices.id) > ?";
                        // Concatenation for all other default fields
                        default:
                            if (filters.get(key) == null) {
                                return key + " IS ?";
                            } else {
                                return key + " LIKE ?";
                            }
                    }
                })
                .collect(Collectors.joining(" AND "));

        if(!conditions.isBlank()) {
            query += "WHERE ";
            query += conditions;
        }

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


    @Override
    public InformativeInvoice getInvoiceDetails(int invoiceId) throws DBException {
        String query = "SELECT tbl_invoices.id AS invoice_id, tbl_invoices.code AS invoice_code,\n" +
                "       tbl_invoices.date_start, tbl_invoices.date_end,\n" +
                "       tbl_invoices.exp_price, tbl_invoices.is_canceled, tbl_invoices.is_rejected,\n" +
                "       tbl_invoices.driver_id, tbl_drivers.code AS driver_code, driver_u.avatar AS driver_avatar,driver_u.email AS driver_email,\n" +
                "       client_u.email AS client_email,\n" +
                "       tbl_cars.brand, tbl_cars.model,\n" +
                "       tbl_passport.firstname AS pp_firstname, tbl_passport.surname AS pp_surname, tbl_passport.patronymic AS pp_patronymic,\n" +
                "       tbl_passport.date_of_birth AS pp_date_of_birth, tbl_passport.date_of_issue AS pp_date_of_issue,\n" +
                "       tbl_passport.doc_number AS pp_doc_number, tbl_passport.rntrc AS pp_rntrc, tbl_passport.authority AS pp_authority, tbl_cars.city_id, tbl_invoices.reject_reason\n" +
                "FROM tbl_invoices\n" +
                "         LEFT JOIN tbl_drivers ON tbl_invoices.driver_id = tbl_drivers.id\n" +
                "         LEFT JOIN tbl_users AS driver_u ON tbl_drivers.user_id = driver_u.id\n" +
                "         JOIN tbl_users AS client_u ON tbl_invoices.client_id = client_u.id\n" +
                "         JOIN tbl_cars ON tbl_invoices.car_id = tbl_cars.id\n" +
                "         JOIN tbl_passport ON tbl_passport.id = tbl_invoices.passport_id WHERE tbl_invoices.id = ?";

        try(
                Connection con = ds.getConnection();
                PreparedStatement globalStmt = con.prepareStatement(query);
        ){
            // Getting all invoice info except repairs invoices (because we can have many of them, so we are going to get them in another query later)
            globalStmt.setInt(1, invoiceId);

            InformativeInvoice informativeInvoice = null;

            try(ResultSet rs = globalStmt.executeQuery()){
                if(rs.next())
                    informativeInvoice = InformativeInvoice.of(rs);
            }

            // Getting repair invoices
            if(informativeInvoice != null){
                try(PreparedStatement obtainRepairsStmt = con.prepareStatement("SELECT * FROM tbl_repair_invoices WHERE invoice_id = ? ORDER BY ts_created DESC")){
                    obtainRepairsStmt.setInt(1, invoiceId);

                    // Obtaining found
                    List<RepairInvoice> repairInvoices = new LinkedList<>();
                    try(ResultSet rs = obtainRepairsStmt.executeQuery()){
                         while(rs.next())
                             repairInvoices.add(RepairInvoice.of(rs));
                    }

                    informativeInvoice.setRepairInvoices(repairInvoices);

                    // Adding statuses (if we didn`t find any repair invoices, no statuses will be added)
                    int activeRepairs = 0;
                    int expiredRepairs = 0;
                    for(RepairInvoice repair : repairInvoices){
                        if(repair.isPaid())
                            continue;

                        if(repair.getExpirationDate().isBefore(LocalDate.now())){
                            expiredRepairs++;
                        }else{
                            activeRepairs++;
                        }
                    }

                    List<InvoiceStatuses> statuses = informativeInvoice.getStatusList(); // getting from object, because in Informative.of() method we might set IS_CANCELLED and IS_REJECTED statuses

                    if(activeRepairs > 0)
                        statuses.add(InvoiceStatuses.ACTIVE_REPAIRS);
                    if(expiredRepairs > 0)
                        statuses.add(InvoiceStatuses.EXPIRED_REPAIRS);

                    informativeInvoice.setStatusList(statuses);
                }
            }

            return informativeInvoice;

        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public void createRepairInvoice(int invoiceId, BigDecimal price, LocalDate expirationDate, String comment) throws DBException {
        try(
              Connection con = ds.getConnection();
              PreparedStatement stmt = con.prepareStatement("INSERT INTO tbl_repair_invoices (invoice_id, price, expiration_date, comment, is_paid) VALUES (?, ?, ?, ?, ?)");
        ){
            int index = 0;
            stmt.setInt(++index, invoiceId);
            stmt.setBigDecimal(++index, price);
            stmt.setObject(++index, expirationDate);
            stmt.setString(++index, comment);
            stmt.setInt(++index, 0);

            stmt.executeUpdate();
        }catch (SQLException e){
            logger.error(e);

        }
    }

    @Override
    public void deleteRepairInvoice(int repairId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("DELETE FROM tbl_repair_invoices WHERE id = ?")
        ){
            stmt.setInt(1, repairId);

            stmt.executeUpdate();
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public Optional<RepairInvoice> getRepairInvoiceInfo(int repairId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement(
                        "SELECT tbl_repair_invoices.*,tbl_users.email AS client_email FROM tbl_repair_invoices \n" +
                        "JOIN tbl_invoices ON tbl_invoices.id = tbl_repair_invoices.invoice_id\n" +
                        "JOIN tbl_users ON tbl_users.id = tbl_invoices.client_id\n" +
                        "WHERE tbl_repair_invoices.id = ?"
                )
        ){
            stmt.setInt(1, repairId);
            Optional<RepairInvoice> repairInvoice = Optional.empty();

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    repairInvoice = Optional.of(RepairInvoice.of(rs));
                    repairInvoice.get().setClientEmail(rs.getString("client_email"));
                }
            }

            return repairInvoice;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public void rejectInvoice(int invoiceId, String reason) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("UPDATE tbl_invoices SET is_rejected = 1, reject_reason = ? WHERE id = ?")
        ){
            stmt.setString(1, reason);
            stmt.setInt(2, invoiceId);

            stmt.executeUpdate();
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }
}
