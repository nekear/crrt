package com.github.DiachenkoMD.web.controllers.manager.invoices;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.web.services.AdminService;
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

import java.io.IOException;

import static com.github.DiachenkoMD.web.utils.Utils.sendSuccess;
@UseGuards({AuthGuard.class, ManagerRGuard.class})
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String paginationWrapperJSON = req.getParameter("data");
            sendSuccess(gson.toJson(managerService.getInvoices(paginationWrapperJSON)), resp);
        }catch (Exception e){
            logger.error(e);

            try {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(new StatusText("global.unexpectedError").convert(Utils.getLang(req)));
                resp.getWriter().flush();
            } catch (IOException ioExc) {
                logger.error(ioExc);
            }
        }
    }
}
