package com.github.DiachenkoMD.web.controllers;

import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.Ordery;
import com.github.DiachenkoMD.entities.dto.PaginationRequest;
import com.github.DiachenkoMD.entities.dto.PaginationResponse;
import com.github.DiachenkoMD.entities.dto.invoices.InvoicePanelFilters;
import com.github.DiachenkoMD.entities.dto.invoices.PanelInvoice;
import com.github.DiachenkoMD.entities.enums.InvoiceStatuses;
import com.github.DiachenkoMD.web.services.ManagerService;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.PageGuard;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.roles.ManagerRGuard;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@UseGuards({PageGuard.class, AuthGuard.class, ManagerRGuard.class})
@WebServlet("/manager")
public class ManagerController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(ManagerController.class);
    private ManagerService managerService;

    private Gson gson;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        managerService = ((ManagerService) config.getServletContext().getAttribute("manager_service"));
        gson = (Gson) config.getServletContext().getAttribute("gson");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        try{
            PaginationRequest paginationRequest = formPaginationRequest(req);

            PaginationResponse<PanelInvoice> foundInvoices = managerService.getInvoices(paginationRequest);

            req.setAttribute("paginationResponse", foundInvoices);

            getServletContext().getRequestDispatcher("/views/manager.jsp").forward(req, resp);
        }catch (Exception e){
            logger.error(e);
        }
    }

    /**
     * Created to take the PaginationRequest object formation logic out of the {@link #doGet(HttpServletRequest, HttpServletResponse) doGet} route. <br/>
     * Never returns fully empty {@link PaginationRequest} object. There always be askedPage field and elementsPerPage. <br/>
     * <strong>askedPage</strong> equals 1 if nothing set and something, if we can obtain that "something" from request. The same logic goes for <strong>elementsPerPage</strong>, but the default value here is 15.<br/>
     * <strong>invoiceFilters</strong> field ({@link InvoicePanelFilters} type) won`t be null ever, but may contain only null fields if they were not specified. No default values set here.
     * <pre>Note: fields like <i>datesRange</i> and <i>orderBy</i> should be in form of JSON, because they are parsed by GSON with {@link DatesRange} and {@link Ordery} classes correspondingly.</pre>
     * @param req
     * @return {@link PaginationRequest} object to pass it then into {@link ManagerService#getInvoices(PaginationRequest)}.
     */
    private PaginationRequest formPaginationRequest(HttpServletRequest req){
        String askedPageStr = req.getParameter("askedPage");
        String elementsPerPageStr = req.getParameter("elementsPerPage");

        PaginationRequest pr = new PaginationRequest();

        int askedPage = askedPageStr == null ? 1 : Integer.parseInt(askedPageStr);
        int elementsPerPage = elementsPerPageStr == null ? 15 : Integer.parseInt(elementsPerPageStr);

        pr.setAskedPage(askedPage);
        pr.setElementsPerPage(elementsPerPage);

        InvoicePanelFilters filters = new InvoicePanelFilters();

        filters.setCode(req.getParameter("code"));
        filters.setCarName(req.getParameter("carName"));
        filters.setDriverEmail(req.getParameter("driverEmail"));
        filters.setClientEmail(req.getParameter("clientEmail"));
        filters.setStatus(
                req.getParameter("status") != null
                        ? InvoiceStatuses.getById(Integer.parseInt(req.getParameter("status")))
                        : InvoiceStatuses.ANY
        );

        filters.setDatesRange(gson.fromJson(
                        req.getParameter("datesRange") != null
                                ? req.getParameter("datesRange")
                                : "",
                        DatesRange.class
                )
        );

        filters.setOrderBy(gson.fromJson(
                        req.getParameter("orderBy") != null
                                ? req.getParameter("orderBy")
                                : "",
                        new TypeToken<List<Ordery>>(){}.getType()
                )
        );

        pr.setInvoicesFilters(filters);

        logger.debug(pr);

        return pr;
    }
}
