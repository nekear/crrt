package com.github.DiachenkoMD.web.controllers.client.invoices;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.enums.InvoiceStatuses;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.services.ClientService;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.github.DiachenkoMD.web.utils.Utils;
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

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.entities.Constants.SESSION_AUTH;
import static com.github.DiachenkoMD.web.utils.Utils.*;
import static com.github.DiachenkoMD.web.utils.Utils.getLang;

@UseGuards({AuthGuard.class, ClientRGuard.class})
@WebServlet("/client/invoice")
public class ClientInvoiceController extends HttpServlet {
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
     * Route for getting invoice details for client. Called, for example, when client clicks "details" on specific invoice on "My invoices" page.
     * @see ClientService#getInvoiceDetails(int)
     * @param req > "invoice_id": String
     * @param resp > {@link com.github.DiachenkoMD.entities.dto.invoices.InformativeInvoice InformativeInvoice}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        try{
            String invoiceIdEncrypted = req.getParameter("invoice_id");

            int invoiceId = Integer.parseInt(CryptoStore.decrypt(invoiceIdEncrypted));

            sendSuccess(gson.toJson(clientService.getInvoiceDetails(invoiceId)), resp);
        }catch (Exception e){
            logger.error(e);

            sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }

    /**
     * Served under <code>DELETE:/client/invoice</code>. Designed to allow client disable their invoices. Should receive <strong>invoice id</strong> inside under <strong>id</strong> field. <br/>
     * NOTE: if successful, returns only id (int) of {@link com.github.DiachenkoMD.entities.enums.InvoiceStatuses#CANCELED CANCELED} status, because on the client side its expected that
     * developer (me) will just add that id to list of status of the invoice and Vue will handle all other rendering-related stuff.
     * @see ClientService#cancelInvoice(int, AuthUser)
     * @param req > <code>{"id"(invoice id): String}</code>
     * @param resp > <code>{"status": int, "newBalance": double}</code>
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String invoiceIdEncrypted = new JSONObject(req.getReader().lines().collect(Collectors.joining())).getString("id");

            int invoiceId = Integer.parseInt(CryptoStore.decrypt(invoiceIdEncrypted));

            AuthUser client = (AuthUser) req.getSession().getAttribute(SESSION_AUTH);

            clientService.cancelInvoice(invoiceId, client);

            sendSuccess(gson.toJson(Map.of("status", InvoiceStatuses.CANCELED, "newBalance", client.getBalance())), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>();

            logger.error(e);

            if (e instanceof DescriptiveException descExc) {
                descExc.execute(ExceptionReason.INVOICE_ALREADY_REJECTED, () -> exceptionToClient.set(new StatusText("client.cancelling.invoice_already_cancelled").convert(getLang(req))));
                descExc.execute(ExceptionReason.INVOICE_ALREADY_REJECTED, () -> exceptionToClient.set(new StatusText("client.cancelling.invoice_already_rejected").convert(getLang(req))));
                descExc.execute(ExceptionReason.INVOICE_ALREADY_STARTED, () -> exceptionToClient.set(new StatusText("client.cancelling.invoice_already_started").convert(getLang(req))));
                descExc.execute(ExceptionReason.INVOICE_ALREADY_EXPIRED, () -> exceptionToClient.set(new StatusText("client.cancelling.invoice_already_expired").convert(getLang(req))));
            }

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            logger.debug(exceptionToClient.get());

            sendException(exceptionToClient.get(), resp);
        }
    }
}
