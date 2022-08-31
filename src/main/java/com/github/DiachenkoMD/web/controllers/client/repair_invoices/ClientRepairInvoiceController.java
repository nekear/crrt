package com.github.DiachenkoMD.web.controllers.client.repair_invoices;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.controllers.client.invoices.ClientInvoiceController;
import com.github.DiachenkoMD.web.services.ClientService;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.roles.ClientRGuard;
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

import static com.github.DiachenkoMD.entities.Constants.SESSION_AUTH;
import static com.github.DiachenkoMD.web.utils.Utils.*;
@UseGuards({AuthGuard.class, ClientRGuard.class})
@WebServlet("/client/repairInvoice")
public class ClientRepairInvoiceController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(ClientInvoiceController.class);
    private ClientService clientService;

    private Gson gson;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        clientService = ((ClientService) config.getServletContext().getAttribute("client_service"));
        gson = (Gson) config.getServletContext().getAttribute("gson");
    }

    /**
     * Served under <code>PUT:/client/repairInvoice</code>. Designed to allow client pay for their repairment invoices.
     * @see ClientService#payRepairmentInvoice(int, AuthUser)
     * @param req > <code>{"id" (repairment id): String}</code>
     * @param resp > {@link com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice InformativeInvoice}
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp){
        try{
            JSONObject jsonBody = new JSONObject(req.getReader().lines().collect(Collectors.joining()));

            int repairInvoiceId = Integer.parseInt(CryptoStore.decrypt(jsonBody.getString("id")));

            AuthUser client = (AuthUser) req.getSession().getAttribute(SESSION_AUTH);

            sendSuccess(gson.toJson(clientService.payRepairmentInvoice(repairInvoiceId, client)), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>();

            logger.error(e);

            if (e instanceof DescriptiveException descExc) {
                descExc.execute(ExceptionReason.REP_INVOICE_WAS_NOT_FOUND, () -> exceptionToClient.set(new StatusText("client.repairment_invoice.not_found").convert(getLang(req))));
                descExc.execute(ExceptionReason.NOT_ENOUGH_MONEY, () -> exceptionToClient.set(new StatusText("client.repairment_invoice.not_enough_money").convert(getLang(req))));
                descExc.execute(ExceptionReason.REP_INVOICE_IS_ALREADY_PAID, () -> exceptionToClient.set(new StatusText("client.repairment_invoice.invoice_is_already_paid").convert(getLang(req))));
            }

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            sendException(exceptionToClient.get(), resp);
        }
    }
}
