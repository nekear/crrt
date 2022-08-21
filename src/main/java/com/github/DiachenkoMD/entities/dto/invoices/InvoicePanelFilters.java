package com.github.DiachenkoMD.entities.dto.invoices;

import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.Filters;
import com.github.DiachenkoMD.entities.dto.Ordery;
import com.github.DiachenkoMD.entities.enums.InvoiceStatuses;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.entities.DB_Constants.*;
import static com.github.DiachenkoMD.web.utils.Utils.*;

public class InvoicePanelFilters extends Filters {

    private String code;
    private String carName;
    private DatesRange datesRange;
    private String driverEmail;
    private String clientEmail;

    private InvoiceStatuses status;

    private List<Ordery> orderBy;

    @Override
    public HashMap<String, String> getDBPresentation() {
        HashMap<String, String> representation = new HashMap<>();


        // Automatically select non-empty strings
        // (from js comes an object where non-empty fields have an empty string, not null, so this cleanup is necessary)
        representation.put("carName", carName);
        representation.put("client_u."+TBL_USERS_EMAIL, clientEmail);

        // the use of the Stream API may look unreasonable here,
        // but it is done so that new simple filters can easily be added in the future
        HashMap<String, String> res = representation.entrySet()
                .parallelStream()
                .filter(x -> x.getValue() != null && !x.getValue().isBlank())
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> "%"+clean(entry.getValue())+"%",
                                (prev, next) -> next,
                                HashMap::new
                        )
                );

        if(driverEmail != null && !driverEmail.isBlank()){
            String cleanDriverEmail = clean(driverEmail);

            if(cleanDriverEmail.equals("-")) {
                res.put("driver_u."+TBL_USERS_EMAIL, null);
            }else{
                res.put("driver_u."+TBL_USERS_EMAIL, "%"+cleanDriverEmail+"%");
            }
        }

        // Statuses
        if(status != null){
            switch (status){
                case ALIVE -> {
                    res.put("tbl_invoices." + TBL_INVOICES_IS_CANCELED, "0");
                    res.put("tbl_invoices." + TBL_INVOICES_IS_REJECTED, "0");
                }
                case CANCELED -> res.put("tbl_invoices." + TBL_INVOICES_IS_CANCELED, "1");
                case REJECTED -> res.put("tbl_invoices." + TBL_INVOICES_IS_REJECTED, "1");
                case ACTIVE_REPAIRS -> res.put("activeRepairs", "0");
                case EXPIRED_REPAIRS -> res.put("expiredRepairs", "0");
            }
        }


        // Code is filtered in db with use of MATCH AGAINST
        // Logically, this operation should be popular in real life, so it is important to provide a fast search speed in the database on a large number of entities,
        // that is why I decided to use MATCH AGAINST and FULLTEXT
        String code = cleanGetString(this.code);
        if(code != null){
            res.put("code", code + "*");
        }



        // Dates should be formatted before use in db query
        if(datesRange != null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            res.put(TBL_INVOICES_DATE_START, formatter.format(datesRange.getStart()));
            res.put(TBL_INVOICES_DATE_END, formatter.format(datesRange.getEnd()));
        }

        return res;
    }

    /**
     * Method for getting matchings of sort params from request to db columns names
     * @return List of ORDER BY ready parameters in form of "ORDER BY _col_ ASC/DESC"
     */
    public List<String> getOrderPresentation(){
        if(orderBy == null || orderBy.size() == 0)
            return null;

        return orderBy.stream()
                .filter(x -> multieq(x.getName(), "carName", "datesRange", "price"))
                .filter(x -> multieq(x.getType(), "asc", "desc"))
                .map( x -> {
                    String name = x.getName();
                    String type = x.getType().toUpperCase();
                    if(name.equalsIgnoreCase("carName")){
                        return String.format("tbl_cars.brand %s, tbl_cars.model %s", type, type);
                    }else if(name.equalsIgnoreCase("datesRange")){
                        return "tbl_invoices.date_start " + type;
                    }else if(name.equalsIgnoreCase("price")){
                        return "tbl_invoices.exp_price " + type;
                    }else{
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("{\n" +
                "   Code: %s,\n" +
                "   Name: %s,\n" +
                "   DatesRange: %s,\n" +
                "   DriverEmail: %s,\n" +
                "   ClientEmail: %s,\n" +
                "   Status: %s\n" +
                "}", this.getCode(), this.getCarName(), this.getDatesRange(), this.getDriverEmail(), this.getClientEmail(), this.getStatus());
    }

    private String dgp(String glued, String str){
        return glued + str + glued;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public DatesRange getDatesRange() {
        return datesRange;
    }

    public void setDatesRange(DatesRange datesRange) {
        this.datesRange = datesRange;
    }

    public String getDriverEmail() {
        return driverEmail;
    }

    public void setDriverEmail(String driverEmail) {
        this.driverEmail = driverEmail;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public InvoiceStatuses getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatuses status) {
        this.status = status;
    }

    public List<Ordery> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(List<Ordery> orderBy) {
        this.orderBy = orderBy;
    }
}
