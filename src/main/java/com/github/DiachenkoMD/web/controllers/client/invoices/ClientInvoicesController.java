package com.github.DiachenkoMD.web.controllers.client.invoices;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.web.services.ClientService;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.guardian.guards.roles.ClientRGuard;
import com.google.gson.Gson;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.github.DiachenkoMD.entities.Constants.SESSION_AUTH;
import static com.github.DiachenkoMD.web.utils.Utils.sendException;
import static com.github.DiachenkoMD.web.utils.Utils.sendSuccess;

@UseGuards({AuthGuard.class, ClientRGuard.class})
@WebServlet("/client/invoices")
public class ClientInvoicesController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(ClientInvoicesController.class);
    private ClientService clientService;

    private Gson gson;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        clientService = ((ClientService) config.getServletContext().getAttribute("client_service"));
        gson = (Gson) config.getServletContext().getAttribute("gson");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        try{
            int clientId = ((AuthUser) req.getSession().getAttribute(SESSION_AUTH)).getCleanId().get();
            sendSuccess(gson.toJson(clientService.getInvoices(clientId)), resp);
        }catch (Exception e){
            logger.error(e);

            sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }

}
