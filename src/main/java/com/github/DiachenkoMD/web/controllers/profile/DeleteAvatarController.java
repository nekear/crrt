package com.github.DiachenkoMD.web.controllers.profile;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.web.services.UsersService;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.AuthGuard;
import com.google.gson.Gson;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.DiachenkoMD.web.utils.Utils.getLang;
import static com.github.DiachenkoMD.web.utils.Utils.sendException;

@UseGuards({AuthGuard.class})
@WebServlet("/profile/deleteAvatar")
public class DeleteAvatarController extends HttpServlet {
    private final static Logger logger = LogManager.getLogger(DeleteAvatarController.class);
    private UsersService usersService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.usersService = (UsersService) config.getServletContext().getAttribute("users_service");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String generatedAvatarUrl = usersService.deleteAvatar(req, resp);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(Map.of("avatar", generatedAvatarUrl)));
            resp.getWriter().flush();

        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            logger.error(e);

            exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            sendException(exceptionToClient.get(), resp);
        }
    }
}
