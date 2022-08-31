package com.github.DiachenkoMD.web.controllers.admin.users_related;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.enums.StatusStates;
import com.github.DiachenkoMD.entities.exceptions.DBException;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.web.utils.Utils.*;
@UseGuards({AuthGuard.class, AdminRGuard.class})
@WebServlet("/admin/user")
public class UserController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(UserController.class);
    private AdminService adminService;
    private Gson gson;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        adminService = ((AdminService) config.getServletContext().getAttribute("admin_service"));
        gson = ((Gson) config.getServletContext().getAttribute("gson"));
    }

    /**
     * Route for creating new user.
     * @see AdminService#createUser(String)
     * @param req > incoming json is parsed with {@link AdminService.CreationUpdatingUserJPC} class, so make sure that request contains all necessary fields. <br/>
     * Note: "firstname", "surname" and "patronymic" are optional.
     * @param resp > message
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String userCreationDataJSON = req.getReader().lines().collect(Collectors.joining());
            logger.debug(userCreationDataJSON);
            adminService.createUser(userCreationDataJSON);
            sendSuccess(new StatusText("admin.user_creation.success", true, StatusStates.SUCCESS).convert(getLang(req)), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>();

            logger.error(e);

            if (e instanceof DescriptiveException descExc) {
                descExc.execute(ExceptionReason.VALIDATION_ERROR, () -> exceptionToClient.set(new StatusText("admin.user_creation.validation_error").convert(getLang(req))));
                descExc.execute(ExceptionReason.EMAIL_EXISTS, () -> exceptionToClient.set(new StatusText("admin.user_creation.email_exists").convert(getLang(req))));
            }

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            sendException(exceptionToClient.get(), resp);
        }
    }

    /**
     * Route for getting detailed information about user. Can be called when admin clicks "details" on user row on admin-panel page.
     * @see AdminService#getUser(String)
     * @param req > "id" (user id): String.
     * @param resp > {@link com.github.DiachenkoMD.entities.dto.users.InformativeUser InformativeUser}.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String userIdEncrypted = req.getParameter("id");
            logger.debug(userIdEncrypted);

            sendSuccess(gson.toJson(adminService.getUser(userIdEncrypted)), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>();

            logger.error(e);

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            sendException(exceptionToClient.get(), resp);
        }
    }

    /**
     * Route for updating user data. Will be called, when admin clicks "save changes" on user.
     * @see AdminService#updateUser(String)
     * @param req > json is parsed with {@link AdminService.CreationUpdatingUserJPC}, so make sure to include all necessary fields. <br/>
     * Note: "firstname", "surname" and "patronymic", as well as "role" should be present always. "Email" and "password" might not be present.
     * @param resp > message
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String changedUserDataJSON = new BufferedReader(req.getReader()).lines().collect(Collectors.joining());

            adminService.updateUser(changedUserDataJSON);

            sendSuccess(new StatusText("admin.user_update.success", true, StatusStates.SUCCESS).convert(getLang(req)), resp);
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

    /**
     * Route for deleting user<strong>s</strong>. Can accept one or many users` ids.
     * @see AdminService#deleteUsers(String)
     * @param req > List<{@linkplain String}> (list of encrypted users` ids to delete)
     * @param resp > nothing
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String idsToDeleteListJSON = req.getReader().lines().collect(Collectors.joining());

            adminService.deleteUsers(idsToDeleteListJSON);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>();

            logger.error(e);

            if(e instanceof DBException){
                exceptionToClient.set(new StatusText("admin.user_deleting.users_coupled").convert(getLang(req)));
            }

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            sendException(exceptionToClient.get(), resp);
        }
    }
}
