package com.github.DiachenkoMD.web.controllers.manager.repair_invoices;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.services.ManagerService;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.guardian.guards.roles.ManagerRGuard;
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
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.web.utils.Utils.*;
import static com.github.DiachenkoMD.web.utils.Utils.getLang;
@UseGuards({AuthGuard.class, ManagerRGuard.class})
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String jsonBody = new BufferedReader(req.getReader()).lines().collect(Collectors.joining());

            sendSuccess(gson.toJson(managerService.createRepairmentInvoice(jsonBody)), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            logger.error(e);

            if (e instanceof DescriptiveException descExc)
                descExc.execute(ExceptionReason.VALIDATION_ERROR, () -> exceptionToClient.set(new StatusText("manager.repair_invoices.creation_validation_fail").convert(getLang(req))));

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try{
            JSONObject jsonBody = new JSONObject(req.getReader().lines().collect(Collectors.joining()));

            String originInvoiceIdEncrypted = jsonBody.getString("originId");
            String repairInvoiceIdEncrypted = jsonBody.getString("repairId");

            sendSuccess(gson.toJson(managerService.deleteRepairmentInvoice(originInvoiceIdEncrypted, repairInvoiceIdEncrypted)), resp);
        }catch (Exception e){
            logger.error(e);

            sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }
}
