package com.github.DiachenkoMD.web.controllers.profile;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.web.services.UsersService;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.AuthGuard;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.github.DiachenkoMD.web.utils.Utils.*;

@UseGuards({AuthGuard.class})
@WebServlet("/profile/theme")
public class ThemeController extends HttpServlet {
    private final static Logger logger = LogManager.getLogger(ThemeController.class);
    private UsersService usersService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.usersService = (UsersService) config.getServletContext().getAttribute("users_service");
    }

    /**
     * Route, designed to change user`s website appearance. Awaits nothing, but for better UX, client side should have Cookie called "theme".
     * @see UsersService#changeTheme(HttpServletRequest, HttpServletResponse)
     * @param req > nothing (should better contain Cookie "theme")
     * @param resp > nothing (updates Cookie "theme")
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        try{
            usersService.changeTheme(req, resp);
        }catch (Exception e){
            logger.error(e);
            sendException(new StatusText("global.unexpectedError").convert(getLang(req)), resp);
        }
    }
}
