package com.github.DiachenkoMD.web.controllers.admin.users_related;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.web.services.AdminService;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.roles.AdminRGuard;
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
@UseGuards({AuthGuard.class, AdminRGuard.class})
@WebServlet("/admin/users")
public class UsersController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(UsersController.class);
    private AdminService adminService;
    private Gson gson;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        adminService = ((AdminService) config.getServletContext().getAttribute("admin_service"));
        gson = ((Gson) config.getServletContext().getAttribute("gson"));
    }

    /**
     * Route for getting all available users with filters in {@link com.github.DiachenkoMD.entities.dto.PaginationRequest PaginationRequest}
     * with {@link com.github.DiachenkoMD.entities.dto.users.UsersPanelFilters UsersPanelFilters} inside. If you need to select all users, just pass empty filters.
     * @see AdminService#getUsers(String)
     * @param req > {@link com.github.DiachenkoMD.entities.dto.PaginationRequest PaginationRequest}
     * with {@link com.github.DiachenkoMD.entities.dto.users.UsersPanelFilters UsersPanelFilters} inside.
     * @param resp > {@link com.github.DiachenkoMD.entities.dto.PaginationResponse PaginationResponse<T>}, where <i>T</i> is {@link com.github.DiachenkoMD.entities.dto.users.PanelUser PanelUser}.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String paginationWrapperJSON = req.getParameter("data");
            sendSuccess(gson.toJson(adminService.getUsers(paginationWrapperJSON)), resp);
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
