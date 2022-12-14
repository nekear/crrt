package com.github.DiachenkoMD.web.controllers.admin.users_related;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.services.AdminService;
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
import org.json.JSONObject;

import java.io.BufferedReader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.web.utils.Utils.*;
@UseGuards({AuthGuard.class, AdminRGuard.class})
@WebServlet("/admin/user/block")
public class UserBlockController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(UserBlockController.class);
    private AdminService adminService;
    private Gson gson;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        adminService = ((AdminService) config.getServletContext().getAttribute("admin_service"));
        gson = ((Gson) config.getServletContext().getAttribute("gson"));
    }

    /**
     * Route for updating user state (blocking / unblocking user).
     * @see AdminService#updateUserState(String, int)
     * @param req > <code>{"id" (user id): String, "newState": int}</code>
     * @param resp > nothing
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String updateStateDataJSON = new BufferedReader(req.getReader()).lines().collect(Collectors.joining());

            JSONObject object = new JSONObject(updateStateDataJSON);

            adminService.updateUserState(object.getString("id"), object.getInt("newState"));
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>();

            logger.error(e);

            if(e instanceof DescriptiveException desExc){
                desExc.execute(ExceptionReason.VALIDATION_ERROR, () -> exceptionToClient.set(new StatusText("admin.user_update.validation_error").convert(getLang(req))));
            }

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            sendException(exceptionToClient.get(), resp);
        }
    }
}
