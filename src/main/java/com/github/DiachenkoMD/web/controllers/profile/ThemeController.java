package com.github.DiachenkoMD.web.controllers.profile;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.services.UsersService;
import com.github.DiachenkoMD.web.utils.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.guardian.guards.AuthGuard;
import com.google.gson.Gson;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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
