package com.github.DiachenkoMD.web.controllers.manager.invoices;

import com.github.DiachenkoMD.entities.dto.PaginationRequest;
import com.github.DiachenkoMD.entities.dto.StatusText;
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

import static com.github.DiachenkoMD.web.utils.Utils.sendException;
import static com.github.DiachenkoMD.web.utils.Utils.sendSuccess;
@UseGuards({AuthGuard.class, HTierRGuard.class})
@WebServlet("/manage/invoices")
public class InvoicesController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(InvoicesController.class);
    private ManagerService managerService;
    private Gson gson;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        managerService = ((ManagerService) config.getServletContext().getAttribute("manager_service"));
        gson = ((Gson) config.getServletContext().getAttribute("gson"));
    }

    /**
     * Route for obtaining invoices list at admin-panel. Can contain empty or filled filters.
     * @see ManagerService#getInvoices(PaginationRequest)
     * @param req > incoming json object should have structure of {@link PaginationRequest} with {@link com.github.DiachenkoMD.entities.dto.invoices.InvoicePanelFilters InvoicePanelFilters}
     * @param resp > {@link com.github.DiachenkoMD.entities.dto.PaginationResponse PaginationResponse<T>} with {@link com.github.DiachenkoMD.entities.dto.invoices.PanelInvoice PanelInvoice}.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String paginationRequestJSON = req.getParameter("data");

            PaginationRequest paginationRequest = gson.fromJson(paginationRequestJSON, PaginationRequest.class);

            sendSuccess(gson.toJson(managerService.getInvoices(paginationRequest)), resp);
        }catch (Exception e){
            logger.error(e);

            sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }
}
