package com.github.DiachenkoMD.web.daos.impls.mysql;

import static com.github.DiachenkoMD.entities.DB_Constants.*;

import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.invoices.*;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.github.DiachenkoMD.entities.enums.InvoiceStatuses;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.web.utils.Utils.generateRandomString;

public class MysqlInvoicesDAO implements InvoicesDAO {

    private static final Logger logger = LogManager.getLogger(MysqlInvoicesDAO.class);
    private final DataSource ds;

    public MysqlInvoicesDAO(DataSource ds){
        this.ds = ds;
    }

    @Override
    public HashMap<Integer, String> getInvoicesToClientsOnCar(int carId) throws DBException {
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
    public List<PanelInvoice> getPanelInvoicesWithFilters(HashMap<String, String> filters, List<String> orderBy, int limitOffset, int limitCount) throws DBException{
        String query = "SELECT tbl_invoices.id AS invoice_id, tbl_invoices.code AS invoice_code,\n" +
                "       tbl_invoices.date_start, tbl_invoices.date_end,\n" +
                "       tbl_invoices.exp_price, tbl_invoices.is_canceled, tbl_invoices.is_rejected,\n" +
                "       getActiveRepairsByInvoiceId(tbl_invoices.id) AS activeRepairs,\n" +
                "       getExpiredRepairsByInvoiceId(tbl_invoices.id) AS expiredRepairs,\n" +
                "       tbl_invoices.driver_id, driver_u.avatar AS driver_avatar,driver_u.email AS driver_email,\n" +
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

        if(orderBy != null && orderBy.size() > 0){
            StringJoiner orderingJoiner = new StringJoiner(",", " ORDER BY ", "");
            orderBy.forEach(orderingJoiner::add);
            query += orderingJoiner.toString();
        }else{
            query += " ORDER BY tbl_invoices.ts_created DESC";
        }


        if(limitCount != -1){
            query += " LIMIT " + limitOffset + ", " + limitCount;
        }

        logger.info(query);

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
                "       tbl_invoices.driver_id, driver_u.avatar AS driver_avatar,driver_u.email AS driver_email,\n" +
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
    public int createRepairInvoice(int invoiceId, BigDecimal price, LocalDate expirationDate, String comment) throws DBException {
        try(
              Connection con = ds.getConnection();
              PreparedStatement stmt = con.prepareStatement("INSERT INTO tbl_repair_invoices (invoice_id, price, expiration_date, comment, is_paid) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ){
            int index = 0;
            stmt.setInt(++index, invoiceId);
            stmt.setBigDecimal(++index, price);
            stmt.setObject(++index, expirationDate);
            stmt.setString(++index, comment);
            stmt.setInt(++index, 0);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public boolean deleteRepairInvoice(int repairId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("DELETE FROM tbl_repair_invoices WHERE id = ?")
        ){
            stmt.setInt(1, repairId);

            return stmt.executeUpdate() > 0;
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
    public boolean rejectInvoice(int invoiceId, String reason) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("UPDATE tbl_invoices SET is_rejected = 1, reject_reason = ? WHERE id = ?")
        ){
            stmt.setString(1, reason);
            stmt.setInt(2, invoiceId);

            return stmt.executeUpdate() > 0;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }


    @Override
    public List<Double> getStats() throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT (SELECT COUNT(id) FROM tbl_invoices WHERE date_start <= ? AND date_end >= ?) AS rentsInProgress,  \n" +
                        "COUNT(id) AS newInvoices,  \n" +
                        "(COALESCE((SELECT SUM(price) FROM tbl_repair_invoices WHERE ts_edited >= ? AND is_paid = 1), 0)/100 * 35)+SUM(exp_price) AS earningsThisMonth\n" +
                        "FROM tbl_invoices WHERE ts_created >= ? AND is_canceled = 0 AND is_rejected = 0")
        ){
            LocalDate firstDayOfMonth = YearMonth.now().atDay(1);

            int index = 0;
            stmt.setObject(++index, LocalDate.now());
            stmt.setObject(++index, LocalDate.now());
            stmt.setObject(++index, firstDayOfMonth);
            stmt.setObject(++index, firstDayOfMonth);

            List<Double> stats = new ArrayList<>();

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    stats.add(rs.getDouble("rentsInProgress"));
                    stats.add(rs.getDouble("newInvoices"));
                    stats.add(rs.getDouble("earningsThisMonth"));
                }
            }

            return stats;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public List<ClientInvoice> getInvoicesForClient(int clientId) throws DBException {
        try(
            Connection con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT tbl_invoices.id AS invoice_id, tbl_invoices.code AS invoice_code, tbl_cars.brand, tbl_cars.model, tbl_invoices.date_start, tbl_invoices.date_end, tbl_invoices.exp_price,  tbl_cars.city_id,\n" +
                    "tbl_invoices.is_canceled, tbl_invoices.is_rejected, getActiveRepairsByInvoiceId(tbl_invoices.id) AS activeRepairs,\n" +
                    "getExpiredRepairsByInvoiceId(tbl_invoices.id) AS expiredRepairs\n, tbl_invoices.driver_id AS driver_id " +
                    "FROM tbl_invoices\n" +
                    "JOIN tbl_cars ON tbl_invoices.car_id = tbl_cars.id\n" +
                    "WHERE tbl_invoices.client_id = ? ORDER BY tbl_invoices.ts_created DESC");
        ){
            stmt.setInt(1, clientId);

            List<ClientInvoice> foundInvoices = new LinkedList<>();
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    foundInvoices.add(ClientInvoice.of(rs));
                }
            }

            return foundInvoices;

        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public List<DriverInvoice> getInvoicesForDriver(int userId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT tbl_invoices.id AS invoice_id, tbl_cars.brand, tbl_cars.model, tbl_invoices.date_start, tbl_invoices.date_end, tbl_invoices.exp_price,  tbl_cars.city_id,\n" +
                        "                        tbl_invoices.is_canceled, tbl_invoices.is_rejected\n" +
                        "                        FROM tbl_invoices\n" +
                        "                        JOIN tbl_cars ON tbl_invoices.car_id = tbl_cars.id\n" +
                        "                        JOIN tbl_drivers ON tbl_drivers.id = tbl_invoices.driver_id\n" +
                        "                        JOIN tbl_users ON tbl_users.id = tbl_drivers.user_id\n" +
                        "                        WHERE tbl_users.id = ? ORDER BY tbl_invoices.date_start DESC");
        ){
            stmt.setInt(1, userId);

            List<DriverInvoice> foundInvoices = new LinkedList<>();
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    foundInvoices.add(DriverInvoice.of(rs));
                }
            }

            return foundInvoices;

        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public boolean payRepairInvoice(int repairInvoiceId) throws DBException {
        try(
            Connection con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE tbl_repair_invoices SET is_paid = 1 WHERE id = ? AND is_paid = 0");
        ){
            stmt.setInt(1, repairInvoiceId);

            return stmt.executeUpdate() > 0;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public boolean cancelInvoice(int invoiceId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("UPDATE tbl_invoices SET is_canceled = 1 WHERE id = ?");
        ){
            stmt.setInt(1, invoiceId);

            return stmt.executeUpdate() > 0;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public int createInvoice(int carId, int clientId, DatesRange range, Passport passport, BigDecimal expectedPrice, Integer driverId) throws DBException {
        try(
            Connection con = ds.getConnection();
        ){
            int newInvoiceId;

            try{
                con.setAutoCommit(false);

                // Generating unique code for invoice
                String generatedCode = null;
                boolean doesCodeExists = false;

                do{
                    generatedCode = generateRandomString(7); // alphabet has 26 letters, so 26^6 will result in possible 308_915_776 codes, so we're extending to 7 letters possible getting 8_031_810_176 codes
                    try(PreparedStatement stmt = con.prepareStatement("SELECT COUNT(id) AS counted FROM tbl_invoices WHERE code=?")){
                        stmt.setString(1, generatedCode);
                        try(ResultSet rs = stmt.executeQuery()){
                            rs.next();
                            doesCodeExists = rs.getInt("counted") > 0;
                        }
                    }
                }while(doesCodeExists);

                // Creating passport to further use newly created id in invoice creation query
                String passportCreationQuery = "INSERT INTO tbl_passport (firstname, surname, patronymic, date_of_birth, date_of_issue, doc_number, rntrc, authority) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

                int newPassportId;
                try(PreparedStatement stmt = con.prepareStatement(passportCreationQuery, Statement.RETURN_GENERATED_KEYS)){
                    int index = 0;

                    stmt.setString(++index, passport.getFirstname());
                    stmt.setString(++index, passport.getSurname());
                    stmt.setString(++index, passport.getPatronymic());
                    stmt.setObject(++index, passport.getDateOfBirth());
                    stmt.setObject(++index, passport.getDateOfIssue());
                    stmt.setObject(++index, passport.getDocNumber());
                    stmt.setObject(++index, passport.getRntrc());
                    stmt.setInt(++index, passport.getAuthority());

                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        rs.next();
                        newPassportId = rs.getInt(1);
                    }
                }

                // Creating invoice in tbl_invoices
                String newInvoiceQuery = "INSERT INTO tbl_invoices (code, car_id, driver_id, client_id, exp_price, date_start, date_end, passport_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";


                try(PreparedStatement stmt = con.prepareStatement(newInvoiceQuery, Statement.RETURN_GENERATED_KEYS)){
                    int index = 0;
                    stmt.setString(++index, generatedCode);
                    stmt.setInt(++index, carId);
                    stmt.setObject(++index, driverId);
                    stmt.setInt(++index, clientId);
                    stmt.setBigDecimal(++index, expectedPrice);
                    stmt.setObject(++index, range.getStart());
                    stmt.setObject(++index, range.getEnd());
                    stmt.setInt(++index, newPassportId);

                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        rs.next();
                        newInvoiceId = rs.getInt(1);
                    }
                }

                // Getting users current balance
                BigDecimal currentBalance = null;
                try(PreparedStatement stmt = con.prepareStatement("SELECT balance FROM tbl_users WHERE id = ?")){
                    stmt.setInt(1, clientId);
                    try(ResultSet rs = stmt.executeQuery()){
                        rs.next();
                        currentBalance = rs.getBigDecimal("balance");
                    }
                }

                // Setting new balance (current - expected price)
                BigDecimal newBalance = currentBalance.subtract(expectedPrice);

                try(PreparedStatement stmt = con.prepareStatement("UPDATE tbl_users SET balance = ? WHERE id = ?")){
                    stmt.setBigDecimal(1, newBalance);
                    stmt.setInt(2, clientId);

                    stmt.executeUpdate();
                }

                con.commit();
                con.setAutoCommit(true);

                return newInvoiceId;
            }catch (SQLException e){
                con.rollback();
                con.setAutoCommit(true);
                throw e;
            }

        }catch (SQLException e){
           logger.error(e);
           throw new DBException(e);
        }
    }

    @Override
    public boolean setInvoiceDriver(int invoiceId, Integer driverId) throws DBException {
        try(
               Connection con = ds.getConnection();
               PreparedStatement stmt = con.prepareStatement("UPDATE tbl_invoices SET driver_id = ? WHERE id = ?");
        ){
            stmt.setObject(1, driverId);
            stmt.setInt(2, invoiceId);

            return stmt.executeUpdate() > 0;

        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }
}
