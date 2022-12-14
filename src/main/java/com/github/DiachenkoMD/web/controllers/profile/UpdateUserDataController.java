package com.github.DiachenkoMD.web.controllers.profile;

import com.github.DiachenkoMD.entities.enums.StatusStates;
import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
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

import java.util.concurrent.atomic.AtomicReference;

import static com.github.DiachenkoMD.entities.Constants.SESSION_AUTH;
import static com.github.DiachenkoMD.web.utils.Utils.*;

@UseGuards({AuthGuard.class})
@WebServlet("/profile/updateData")
public class UpdateUserDataController extends HttpServlet {
    private final static Logger logger = LogManager.getLogger(UpdateUserDataController.class);
    private UsersService usersService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.usersService = (UsersService) config.getServletContext().getAttribute("users_service");
    }

    /**
     * Serves /profiel/updateData POST queries. Designed to update users data on profile page, such as firstname, surname, patronymic.
     * @apiNote Expected that client-side will send only changed fields.
     * @see UsersService#updateData(HttpServletRequest, HttpServletResponse)
     * @param req > <code>{"firstname"?: String, "surname"?: String, "patronymic"?: String}</code>
     * @param resp > message
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try{
            AuthUser updated = usersService.updateData(req, resp);

            req.getSession().setAttribute(SESSION_AUTH, updated);

            sendSuccess(new StatusText("profile.data_changed_successfully", true, StatusStates.SUCCESS).convert(getLang(req)), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            logger.error(e);

            if (e instanceof DescriptiveException descExc) {
                descExc.execute(ExceptionReason.VALIDATION_ERROR, () -> exceptionToClient.set(new StatusText("profile.validation_failed").convert(getLang(req))));
            }

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            logger.debug(exceptionToClient.get());

            sendException(exceptionToClient.get(), resp);
        }
    }
}
