package com.github.DiachenkoMD.web.controllers.manager.repair_invoices;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.services.ManagerService;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.roles.HTierRGuard;
import com.google.gson.Gson;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.web.utils.Utils.*;
import static com.github.DiachenkoMD.web.utils.Utils.getLang;
@UseGuards({AuthGuard.class, HTierRGuard.class})
@WebServlet("/manage/repairInvoice")
public class RepairInvoiceController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(RepairInvoiceController.class);
    private ManagerService managerService;
    private Gson gson;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        managerService = ((ManagerService) config.getServletContext().getAttribute("manager_service"));
        gson = ((Gson) config.getServletContext().getAttribute("gson"));
    }

    /**
     * Route for creating repairment invoices.
     * @see ManagerService#createRepairmentInvoice(String)
     * @param req > json is being parsed with {@link ManagerService.CreateRepairmentInvoiceJPC} class, so it should contain all necessary fields.
     * @param resp > {@link com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice InformativeInvoice}
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String jsonBody = new BufferedReader(req.getReader()).lines().collect(Collectors.joining());

            sendSuccess(gson.toJson(managerService.createRepairmentInvoice(jsonBody)), resp);
        }catch (Exception e) {
            AtomicReference<String> exceptionToClient = new AtomicReference<>();

            logger.error(e);

            if (e instanceof DescriptiveException descExc){
                descExc.execute(ExceptionReason.INVOICE_ALREADY_CANCELLED, () -> exceptionToClient.set(new StatusText("manager.repair_invoices.invoice_already_cancelled").convert(getLang(req))));
                descExc.execute(ExceptionReason.INVOICE_ALREADY_REJECTED, () -> exceptionToClient.set(new StatusText("manager.repair_invoices.invoice_already_rejected").convert(getLang(req))));
                descExc.execute(ExceptionReason.VALIDATION_ERROR, () -> exceptionToClient.set(new StatusText("manager.repair_invoices.creation_validation_fail").convert(getLang(req))));
                descExc.execute(ExceptionReason.REP_INVOICE_EXPIRATION_SHOULD_BE_LATER, () -> exceptionToClient.set(new StatusText("manager.repair_invoices.expiration_date_should_be_greater").convert(getLang(req))));
            }

            if(exceptionToClient.get() == null)
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            sendException(exceptionToClient.get(), resp);
        }
    }

    /**
     * Route for deleting repairment invoices. <br/>
     * @see ManagerService#deleteRepairmentInvoice(String)
     * @param req > <code>{"repairId": String}</code>
     * @param resp > {@link com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice InformativeInvoice}
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try{
            JSONObject jsonBody = new JSONObject(req.getReader().lines().collect(Collectors.joining()));

            String repairInvoiceIdEncrypted = jsonBody.getString("repairId");

            sendSuccess(gson.toJson(managerService.deleteRepairmentInvoice(repairInvoiceIdEncrypted)), resp);
        }catch (Exception e){
            logger.error(e);

            sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }
}
