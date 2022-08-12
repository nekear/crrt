package com.github.DiachenkoMD.web.services;

import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.dto.*;
import com.github.DiachenkoMD.entities.dto.invoices.PanelInvoice;
import com.github.DiachenkoMD.entities.dto.users.InformativeUser;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.dto.users.PanelUser;
import com.github.DiachenkoMD.entities.enums.AccountStates;
import com.github.DiachenkoMD.entities.enums.Roles;
import com.github.DiachenkoMD.entities.enums.ValidationParameters;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.google.gson.Gson;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.entities.Constants.IMAGES_UPLOAD_DIR;
import static com.github.DiachenkoMD.web.utils.Utils.*;

public class ManagerService {

    private static final Logger logger = LogManager.getLogger(ManagerService.class);
    private final UsersDAO usersDAO;
    private final CarsDAO carsDAO;
    private final InvoicesDAO invoicesDAO;

    private final ServletContext ctx;

    public ManagerService(UsersDAO usersDAO, CarsDAO carsDAO, InvoicesDAO invoicesDAO, ServletContext ctx){
        this.usersDAO = usersDAO;
        this.carsDAO = carsDAO;
        this.invoicesDAO = invoicesDAO;
        this.ctx = ctx;
    }


    /**
     * Method for acquiring invoices list used at admin-panel and manager-panel. <br/>
     * Incoming json object should have structure of {@link PaginationRequest} with {@link com.github.DiachenkoMD.entities.dto.invoices.InvoicePanelFilters InvoicePanelFilters}
     * @param paginationRequestJSON
     * @return
     */
    public PaginationResponse<PanelInvoice> getInvoices(String paginationRequestJSON) throws DBException {
        Gson gson = (Gson) ctx.getAttribute("gson");
        PaginationRequest pr = gson.fromJson(paginationRequestJSON, PaginationRequest.class);

        logger.debug(pr);

        int askedPage = pr.getAskedPage();
        int elementsPerPage = pr.getElementsPerPage();

        int limitOffset = (askedPage - 1) * elementsPerPage;
        int limitCount = elementsPerPage;

        HashMap<String, String> searchCriteria = pr.getInvoicesFilters().getDBPresentation();

        logger.info(searchCriteria);

        PaginationResponse<PanelInvoice> paginationResponse = new PaginationResponse<>();
        paginationResponse.setResponseData(invoicesDAO.getPanelInvoicesWithFilters(searchCriteria, limitOffset, limitCount));
        paginationResponse.setTotalElements(invoicesDAO.getPanelInvoicesNumberWithFilters(searchCriteria));

        return paginationResponse;
    }
}
