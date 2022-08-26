package com.github.DiachenkoMD.web.services;

import com.github.DiachenkoMD.entities.adapters.CryptoAdapter;
import com.github.DiachenkoMD.entities.dto.*;
import com.github.DiachenkoMD.entities.dto.drivers.LimitedDriver;
import com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.PanelInvoice;
import com.github.DiachenkoMD.entities.dto.invoices.RepairInvoice;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.enums.InvoiceStatuses;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.github.DiachenkoMD.web.utils.RightsManager;
import com.google.gson.Gson;
import com.google.gson.annotations.JsonAdapter;
import jakarta.servlet.ServletContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.web.utils.Utils.*;

public class ManagerService {

    private static final Logger logger = LogManager.getLogger(ManagerService.class);
    private static final Marker DB_MARKER = MarkerManager.getMarker("DB");
    private final UsersDAO usersDAO;
    private final InvoicesDAO invoicesDAO;

    private final ServletContext ctx;

    private final RightsManager rightsManager;

    public ManagerService(UsersDAO usersDAO, InvoicesDAO invoicesDAO, ServletContext ctx){
        this.usersDAO = usersDAO;
        this.invoicesDAO = invoicesDAO;
        this.ctx = ctx;
        this.rightsManager = (RightsManager) ctx.getAttribute("rights_manager");
    }


    /**
     * Method for acquiring invoices list used at admin-panel and manager-panel. <br/>
     * @param paginationRequest
     * @return {@link PaginationResponse<PanelInvoice>} with {@link PanelInvoice} list inside entities field.
     */
    public PaginationResponse<PanelInvoice> getInvoices(PaginationRequest paginationRequest) throws DBException {
        logger.debug(paginationRequest);

        int askedPage = paginationRequest.getAskedPage();
        int elementsPerPage = paginationRequest.getElementsPerPage();

        int limitOffset = (askedPage - 1) * elementsPerPage;
        int limitCount = elementsPerPage;

        HashMap<String, String> searchCriteria = paginationRequest.getInvoicesFilters().getDBPresentation();

        logger.info(searchCriteria);

        List<String> orderBy = paginationRequest.getInvoicesFilters().getOrderPresentation();

        PaginationResponse<PanelInvoice> paginationResponse = new PaginationResponse<>();
        paginationResponse.setResponseData(invoicesDAO.getPanelInvoicesWithFilters(searchCriteria, orderBy, limitOffset, limitCount));
        paginationResponse.setTotalElements(invoicesDAO.getPanelInvoicesNumberWithFilters(searchCriteria));

        return paginationResponse;
    }

    /**
     * Method for acquiring invoice details, including Passport data and Repairment invoices. Designed for use in admin-panel and manager-panel, when clicking on invoice row in data-tables.
     * @param invoiceIdEncrypted encrypted invoice id.
     * @return {@link InformativeInvoice}
     * @throws DescriptiveException
     * @throws DBException
     */
    public InformativeInvoice getInvoiceDetails(String invoiceIdEncrypted) throws DescriptiveException, DBException {
        int invoiceId = Integer.parseInt(CryptoStore.decrypt(invoiceIdEncrypted));

        return invoicesDAO.getInvoiceDetails(invoiceId);
    }

    /**
     * Method for creating repairment invoices. Inside chained with {@link InvoicesDAO#getInvoiceDetails(int)} to return updated invoice data and reload it on the admin`s/manager`s page.
     * @param jsonBody should contain originId! (id of the invoice for which we create repairment invoice), price!, expirationDate! and comment?.
     * @return {@link InformativeInvoice} with updated invoice data
     * @throws DescriptiveException with {@link ExceptionReason#VALIDATION_ERROR VALIDATION_ERROR} or {@link ExceptionReason#REP_INVOICE_EXPIRATION_SHOULD_BE_LATER REP_INVOICE_EXPIRATION_SHOULD_BE_LATER}.
     * @throws DBException caused by {@link InvoicesDAO#createRepairInvoice(int, BigDecimal, LocalDate, String) createRepairInvoice} or {@link InvoicesDAO#getInvoiceDetails(int) getInvoiceDetails}
     */
    public InformativeInvoice createRepairmentInvoice(String jsonBody) throws DescriptiveException, DBException {
        Gson gson = (Gson) ctx.getAttribute("gson");

        CreateRepairmentInvoiceJPC parsedBody = gson.fromJson(jsonBody, CreateRepairmentInvoiceJPC.class);

        int invoiceId = (Integer) parsedBody.getOriginId();
        BigDecimal price = parsedBody.getPrice();
        LocalDate expirationDate = parsedBody.getExpirationDate();
        String comment = parsedBody.getComment();

        if(price == null || price.compareTo(BigDecimal.valueOf(0)) <= 0 || expirationDate == null)
            throw new DescriptiveException("Price or expiration can`t pass validation!", ExceptionReason.VALIDATION_ERROR);

        if(expirationDate.isBefore(LocalDate.now()))
            throw new DescriptiveException("Repairment invoice expiration date should be greater than previuos date", ExceptionReason.REP_INVOICE_EXPIRATION_SHOULD_BE_LATER);

        invoicesDAO.createRepairInvoice(invoiceId, price, expirationDate, comment);

        return invoicesDAO.getInvoiceDetails(invoiceId);
    }

    /**
     * Method for deleting repairment invoices. Used only at admin-panel and manager-panel. It will refund money if repairment invoice has been paid and client hasn`t been blocked. Although, notification about refund will be sent to client`s email.
     * @param repairInvoiceIdEncrypted encrypted repairment invoice id.
     * @return {@link InformativeInvoice} with updated invoice data.
     */
    public InformativeInvoice deleteRepairmentInvoice(String repairInvoiceIdEncrypted) throws DescriptiveException, DBException {
        int repairInvoiceId = Integer.parseInt(CryptoStore.decrypt(repairInvoiceIdEncrypted));

        // Getting repairment invoice data to inform client about it`s deletion if needed (in that case money will be returned)
        RepairInvoice repairInvoice = invoicesDAO.getRepairInvoiceInfo(repairInvoiceId).orElseThrow(() -> new DescriptiveException("Failed to get repairment invoice data from db", ExceptionReason.ACQUIRING_ERROR));

        // Deleting repairment invoice entry from db
        invoicesDAO.deleteRepairInvoice(repairInvoiceId);
        logger.info(DB_MARKER, "Repairment invoice [#{}] deleted successfully from {}.", repairInvoice.getId(), repairInvoice.getClientEmail());
        // Deciding whether we should notify user or not
        if(repairInvoice.isPaid()){
            AuthUser client = usersDAO.get(repairInvoice.getClientEmail());

            // If we blocked user, there is no sense to send him notification or refund money :)
            if(!client.isBlocked()){
                BigDecimal newBalance = BigDecimal.valueOf(client.getBalance()).add(repairInvoice.getPrice()); // should better use BigDecimal for balance from the start but now it`s too late to change it
                usersDAO.setBalance((Integer) client.getId(), newBalance);
                emailNotify(client, "Refund for cancelled repairment invoice", "We apologize for the inconvenience. Apparently, this repairment invoice was billed by mistake, but since you paid for it, we gave you your money back.");
                logger.info(DB_MARKER, "Money refunded from repairment invoice [#{}] to {} at amount of {}$.", repairInvoice.getId(), repairInvoice.getClientEmail(), repairInvoice.getPrice());

                // Adding user id to updating queue (to update his rights on any next action)
                rightsManager.add((Integer) client.getId());
            }
        }

        return invoicesDAO.getInvoiceDetails(repairInvoice.getOriginInvoiceId());
    }

    public InformativeInvoice rejectInvoice(String invoiceIdEncrypted, String rejectionReason) throws DescriptiveException, DBException {
        int invoiceId = Integer.parseInt(CryptoStore.decrypt(invoiceIdEncrypted));

        // Getting informative invoice to check some data
        InformativeInvoice informativeInvoice = invoicesDAO.getInvoiceDetails(invoiceId);

        // Checking whether it was already cancelled
        if(informativeInvoice.getStatusList().contains(InvoiceStatuses.CANCELED))
            throw new DescriptiveException("Unable to reject invoice, because it has been already cancelled by client!", ExceptionReason.INVOICE_ALREADY_CANCELLED);

        // Checking whether it was already rejected or not
        if(informativeInvoice.getStatusList().contains(InvoiceStatuses.REJECTED))
            throw new DescriptiveException("Unable to reject invoice, because it has been already rejected!", ExceptionReason.INVOICE_ALREADY_REJECTED);

        // if invoice is active (it is already started) or his dates range has past, then there is no any sense to reject it
        if(informativeInvoice.getDatesRange().getEnd().isBefore(LocalDate.now()))
            throw new DescriptiveException("Unable to reject invoice because, it has already expired!", ExceptionReason.INVOICE_ALREADY_EXPIRED);

        if(informativeInvoice.getDatesRange().getStart().isBefore(LocalDate.now().plusDays(1)))
            throw new DescriptiveException("Unable to reject invoice because, it has already started!", ExceptionReason.INVOICE_ALREADY_STARTED);

        invoicesDAO.rejectInvoice(invoiceId, rejectionReason);

        // Informing user that his invoice was rejected
        String clientEmail = informativeInvoice.getClientEmail();
        AuthUser client = usersDAO.get(clientEmail);
        // If user not blocked otherwise there is no sense to notify him and make a refund
        if(!client.isBlocked()){
            usersDAO.setBalance((Integer) client.getId(),client.getBalance() + informativeInvoice.getPrice().doubleValue()); // should better use BigDecimal for balance from the start but now its too late to change it
            emailNotify(client, "Refund for rejected invoice",
                    String.format("Your car order has been rejected. Reason: %s. Car: %s. The money in the amount of %s has been returned to your balance. We apologize for the inconvenience.",
                            rejectionReason, informativeInvoice.getBrand() + informativeInvoice.getModel(), informativeInvoice.getPrice()));

            logger.info(DB_MARKER, "Money refunded from invoice [#{}] to {} at amount of {}$. Reason: {}.", informativeInvoice.getId(), client.getEmail(), informativeInvoice.getPrice(), rejectionReason);

            // Adding user id to updating queue (to update his balance)
            rightsManager.add((Integer) client.getId());
        }

        // If invoice had connected driver, we should notify him that he was disconnected him from that invoice and returned to drivers pool
        if(informativeInvoice.getDriver() != null){
            LimitedDriver driver = informativeInvoice.getDriver();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String dateStart = informativeInvoice.getDatesRange().getStart().format(formatter);
            String dateEnd = informativeInvoice.getDatesRange().getEnd().format(formatter);

            emailNotify(informativeInvoice.getDriver().getEmail(), "Invoice unbinding", String.format("Good afternoon, driver %s. You have been disconnected from the invoice scheduled on %s to %s.", driver.getEmail(), dateStart, dateEnd));
        }

        // Updating informative invoice entity and returning to client
        informativeInvoice.getStatusList().add(InvoiceStatuses.REJECTED);
        informativeInvoice.setRejectionReason(rejectionReason);

        return informativeInvoice;
    }

    /**
     * Method for generating invoices reports to Excel file. <br/>
     * All configuration is specified at <strong>xlsxParser.json</strong> file at the root folder.
     * @implSpec Talking about xlsxParser.json format, here are some tips:
     * <ul>
     *     <li><strong>Placeholder</strong> - name of the sheet.</li>
     *     <li><strong>Name</strong> - name of the column in the specified sheet.</li>
     *     <li><strong>Ref</strong> - name of the field from which data for column should be acquired.
     *     <p>Take into consideration, that system uses reflection to succeed in outputting data.
     *     <p>If fields are nested, separate them by <strong>=></strong> sign. </p>
     *     <p>If value should be acquired from some specific function, you can prefix that call with <strong>"call:"</strong>
     *     and everything will work as you expect. </p>
     *     </li>
     * </ul>
     * @return Apache POI Workbook references that should be further written into some output stream, for example.
     * @throws DBException
     */
    public Workbook generateInvoicesReport() throws DBException {
        Workbook workbook = new XSSFWorkbook();

        Sheet invoicesSheet = null, passportsSheet = null, repairsSheet = null;

        // Getting json object with data
        JSONObject xlsxParsingConfiguration = new JSONObject(
                new BufferedReader(
                        new InputStreamReader(
                                getClass().getClassLoader().getResourceAsStream("xlsxParser.json")
                        )
                ).lines().collect(Collectors.joining())
        );

        // Configuring columns for different sheets
        Map<String, String> invoiceCols = new LinkedHashMap<>();
        Map<String, String> passportCols = new LinkedHashMap<>();
        Map<String, String> repairsCols = new LinkedHashMap<>();

        // Setting up some key-value pairs to reduce code amount
        Map<String, Map<String, String>> sheetsFit = new LinkedHashMap<>();
        sheetsFit.put("invoices", invoiceCols);
        sheetsFit.put("passports", passportCols);
        sheetsFit.put("repairs", repairsCols);

        // Parsing data from json configuration file
        for(Map.Entry<String, Map<String, String>> colsStore : sheetsFit.entrySet()){
            String key = colsStore.getKey(); // contains static key value (not reflected in Excel file, so could be anything)
            Map<String, String> container = colsStore.getValue(); // Contains container reference. Needed to reduce amount of code.

            // Parsing our sheets configs
            JSONObject sheetConfig = xlsxParsingConfiguration.getJSONObject(key);

            // Getting sheet placeholder (written inside sheet name in Excel)
            String sheetPlaceholder = sheetConfig.getString("placeholder");

            // Getting columns configuration. Basically, it`s an array of objects with "name" and "ref" fields.
            JSONArray sheetColumnsConfig = sheetConfig.getJSONArray("columns");

            // Creating sheets
            switch (key) {
                case "invoices" -> invoicesSheet = workbook.createSheet(sheetPlaceholder);
                case "passports" -> passportsSheet = workbook.createSheet(sheetPlaceholder);
                case "repairs" -> repairsSheet = workbook.createSheet(sheetPlaceholder);
            }

            // Loading configuration to containers (further will be parsed inside fillRow method)
            for(Object invoiceConfigItem : sheetColumnsConfig){
                JSONObject configItem = (JSONObject) invoiceConfigItem;
                try{
                    String name = configItem.getString("name");
                    String ref = configItem.getString("ref");

                    container.put(name, ref);
                }catch (Exception e){
                    logger.warn("[XLSX PARSER]<{}>: {}. Skipping...", key, e.getMessage());
                }
            }
        }

        // Creating styles (like Header style and some styles for highlighted cells)
        CellStyle headerStyle = workbook.createCellStyle();

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        headerStyle.setFont(font);

        CellStyle highlightStyle = workbook.createCellStyle();
        highlightStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        highlightStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());

        HashMap<CellStylesNames, CellStyle> stylesMap = new HashMap<>(
                Map.of(
                        CellStylesNames.HEADER, headerStyle,
                        CellStylesNames.HIGHLIGHT, highlightStyle
                )
        );


        // Rendering headers for invoices sheet, passports sheet and repairs sheet
        fillHeader(invoiceCols, invoicesSheet, stylesMap);
        fillHeader(passportCols, passportsSheet, stylesMap);
        fillHeader(repairsCols, repairsSheet, stylesMap);

        // Getting invoices data
        List<InformativeInvoice> invoices = invoicesDAO.getInvoicesForReport();

        int activeInvoiceNPassportRow = 1;
        int activeRepairInvoiceRow = 1;
        for(InformativeInvoice invoice : invoices){
            // Rendering invoice row
            Row invoiceRow = invoicesSheet.createRow(activeInvoiceNPassportRow);
            fillRow(invoice, invoiceCols, invoicesSheet, invoiceRow, stylesMap);

            // Rendering passport row
            Row passportRow = passportsSheet.createRow(activeInvoiceNPassportRow);
            fillRow(invoice.getPassport(), passportCols, passportsSheet, passportRow, stylesMap);

            ++activeInvoiceNPassportRow;

            // Rendering connected repairment invoices rows
            if(invoice.getRepairInvoices() != null){
                for(RepairInvoice repairInvoice : invoice.getRepairInvoices()){
                    Row repairInvoiceRow = repairsSheet.createRow(activeRepairInvoiceRow);
                    fillRow(repairInvoice, repairsCols, repairsSheet, repairInvoiceRow, stylesMap);
                    ++activeRepairInvoiceRow;
                }
                ++activeRepairInvoiceRow;
            }
        }

        return workbook;
    }

    /**
     * Helper-method for filling header columns.
     * @implNote This implementation loops through colsData keySet and filling columns in the specified order and
     * adds header style from stylesMap to each column cell.
     * @param colsData map of [String] column name and [String] reflective (omitted inside method) reference.
     * @param sheet Excel sheet reference.
     * @param stylesMap map with styles. Should contain {@link CellStylesNames#HEADER HEADER} style.
     */
    private void fillHeader(Map<String, String> colsData, Sheet sheet, Map<CellStylesNames, CellStyle> stylesMap){
        CellStyle headerStyle = stylesMap.get(CellStylesNames.HEADER);

        Row headerRow = sheet.createRow(0);

        AtomicInteger colIndex = new AtomicInteger(0);

        colsData.keySet().forEach(cellName -> {
            // Setting columns width
            sheet.setColumnWidth(colIndex.get(), cellName.length() * (cellName.length() > 5 ? 400 : 1000));

            // Creating our cell and adding text with some Header-specific styles
            Cell currentCell = headerRow.createCell(colIndex.get());
            currentCell.setCellValue(cellName);
            currentCell.setCellStyle(headerStyle);

            colIndex.set(colIndex.get() + 1);
        });
    }

    /**
     * Helper-method for filling rows. I decided not to create lots of if-else clauses so developed method, which
     * uses reflection inside to acquire data from incoming objects.
     * @implSpec colsData parameter, if contains references that points to nested elements, should have them separated by <strong>=></strong> symbol. <br/>
     * If you need to call method without adding "get" to it, you can write <strong>call:</strong> before that method, and it will be invoked just as is.
     * @param obj the object to be dismantled.
     * @param colsData map of [String] column name (omitted inside) and [String] reflective reference.
     * @param sheet Excel sheet reference.
     * @param currentRow current row reference.
     * @param stylesMap map with styles. Should contain {@link CellStylesNames#HIGHLIGHT HIGHLIGHT} style. Applies to boolean types equal to <i>true</i>.
     */
    private <T> void fillRow(T obj, Map<String, String> colsData, Sheet sheet, Row currentRow, Map<CellStylesNames, CellStyle> stylesMap){
        // Index for counting already filled columns
        AtomicInteger colIndex = new AtomicInteger(0);

        // Looping through reflective references
        colsData.values().forEach(reflectiveRef -> {
            Cell currentCell = currentRow.createCell(colIndex.get());

            // Splitting references to know how deep we should go inside to acquire needed value
            String[] refsArr = reflectiveRef.split("=>");

            // Contains needed (basically last value that we have acquired) value
            Object refData = obj;

            // Going deeper inside references nesting (to prevent NullPointerException, adding additional refData != null condition)
            for(int i = 0; i < refsArr.length && refData != null; i ++) {
                Method getterMethod; // contains our method to get value (might be getXXX or isXXX [if "call:" specified])
                String actualFieldRef = refsArr[i];
                try {
                    String methodName;
                    // if contains "call:" that we should call that method without any modifications, otherwise we should capitalize reference and add "get" prefix to it.
                    if(actualFieldRef.startsWith("call:")){
                        methodName = actualFieldRef.substring(actualFieldRef.indexOf(":")+1);
                    }else{
                        methodName = "get" + capitalize(actualFieldRef);
                    }

                    // Finally, getting our value
                    getterMethod = refData.getClass().getMethod(methodName);
                    refData = getterMethod.invoke(refData);
                }catch (Exception e){
                    throw new RuntimeException(e);
                }
            }

            // Our value may be LocalDate(Time) format or just a boolean value, so we have to correctly format them
            String outputValue;

            if(refData instanceof LocalDateTime ldt){
                outputValue = localDateTimeFormatter.format(ldt);
            }else if(refData instanceof LocalDate ld){
                outputValue = localDateFormatter.format(ld);
            }else if(refData instanceof Boolean boolVal){
                outputValue = boolVal ? "+" : "";

                if(boolVal)
                    currentCell.setCellStyle(stylesMap.get(CellStylesNames.HIGHLIGHT));
            }else{
                outputValue = refData == null ? "" : String.valueOf(refData);
            }

            // Setting data to cell
            currentCell.setCellValue(outputValue);

            // Moving to the next column
            colIndex.set(colIndex.get() + 1);
        });
    }

    private enum CellStylesNames{
        HEADER,
        HIGHLIGHT
    }

    /**
     * JSON Parsing Class (JPC) for GSON. Created for parsing incoming json data for repairment invoice creation at {@link #createRepairmentInvoice(String)}. <br/>
     */
    public static class CreateRepairmentInvoiceJPC{
        @JsonAdapter(CryptoAdapter.class)
        private Object originId; // id of invoice which will be "parent" to repairment invoice

        private BigDecimal price;

        private LocalDate expirationDate;

        private String comment;

        public Object getOriginId() {
            return originId;
        }

        public void setOriginId(Object originId) {
            this.originId = originId;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public LocalDate getExpirationDate() {
            return expirationDate;
        }

        public void setExpirationDate(LocalDate expirationDate) {
            this.expirationDate = expirationDate;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }
}
