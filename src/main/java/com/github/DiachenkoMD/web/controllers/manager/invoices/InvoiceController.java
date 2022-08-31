package com.github.DiachenkoMD.web.controllers.manager.invoices;

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

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.web.utils.Utils.*;
import static com.github.DiachenkoMD.web.utils.Utils.getLang;
@UseGuards({AuthGuard.class, HTierRGuard.class})
@WebServlet("/manage/invoice")
public class InvoiceController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(InvoiceController.class);
    private ManagerService managerService;
    private Gson gson;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        managerService = ((ManagerService) config.getServletContext().getAttribute("manager_service"));
        gson = ((Gson) config.getServletContext().getAttribute("gson"));
    }

    /**
     * Route for getting detailed information about specified invoice id. Used at admin-panel and manager-panel to get details about invoice, when clicking on it.
     * @param req > "invoice_id": String.
     * @param resp > {@link com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice InformativeInvoice}.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String invoiceIdEncrypted = req.getParameter("invoice_id");
            sendSuccess(gson.toJson(managerService.getInvoiceDetails(invoiceIdEncrypted)), resp);
        }catch (Exception e){
            logger.error(e);

            sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }

    /**
     * Served under <code>DELETE:/manage/invoice</code>. Despite its naming, this route was developed for rejecting invoices.
     * @see ManagerService#rejectInvoice(String, String)
     * @param req > <code>{"id" (invoice id): String, "reason"?: String}</code>
     * @param resp > {@link com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice InformativeInvoice}
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try{
            JSONObject jsonBody = new JSONObject(req.getReader().lines().collect(Collectors.joining()));

            String invoiceIdEncrypted = jsonBody.getString("id");

            String rejectionReason = null;
            try{
                rejectionReason = jsonBody.getString("reason");
            }catch (Exception ignored){};

            sendSuccess(gson.toJson(managerService.rejectInvoice(invoiceIdEncrypted, rejectionReason)), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>();

            logger.error(e);

            if (e instanceof DescriptiveException descExc) {
                descExc.execute(ExceptionReason.INVOICE_ALREADY_CANCELLED, () -> exceptionToClient.set(new StatusText("manager.rejection.invoice_already_cancelled").convert(getLang(req))));
                descExc.execute(ExceptionReason.INVOICE_ALREADY_REJECTED, () -> exceptionToClient.set(new StatusText("manager.rejection.invoice_already_rejected").convert(getLang(req))));
                descExc.execute(ExceptionReason.INVOICE_ALREADY_STARTED, () -> exceptionToClient.set(new StatusText("manager.rejection.invoice_already_started").convert(getLang(req))));
                descExc.execute(ExceptionReason.INVOICE_ALREADY_EXPIRED, () -> exceptionToClient.set(new StatusText("manager.rejection.invoice_already_expired").convert(getLang(req))));
            }

            if(exceptionToClient.get() == null)
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            logger.debug(exceptionToClient.get());

            sendException(exceptionToClient.get(), resp);
        }
    }
}
