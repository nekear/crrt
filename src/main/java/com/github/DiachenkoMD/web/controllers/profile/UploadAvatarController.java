package com.github.DiachenkoMD.web.controllers.profile;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.services.UsersService;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.AuthGuard;
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
@WebServlet("/profile/uploadAvatar")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1 MB
        maxFileSize = 1024 * 1024 * 20,      // 20 MB
        maxRequestSize = 1024 * 1024 * 100  // 100 MB
)
public class UploadAvatarController extends HttpServlet {
    private final static Logger logger = LogManager.getLogger(UploadAvatarController.class);
    private UsersService usersService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.usersService = (UsersService) config.getServletContext().getAttribute("users_service");
    }

    /**
     * Serves /profile/uploadAvatar queries. The purpose of this method is to upload the user's avatar to the server.
     * @see UsersService#uploadAvatar(HttpServletRequest, HttpServletResponse)
     * @param req > incoming multipart-form data should contain "avatar" fields inside.
     * @param resp > <code>{"avatar": String}</code>
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String avatarFileName = usersService.uploadAvatar(req, resp);

            resp.setContentType("application/json");
            sendSuccess(new Gson().toJson(Map.of("avatar", req.getContextPath()+"/uploads/avatars/"+avatarFileName)), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            logger.error(e);

            if(e instanceof DescriptiveException desExc){
                desExc.execute(ExceptionReason.BAD_FILE_EXTENSION, () -> exceptionToClient.set(new StatusText("profile.wrong_file_format").convert(getLang(req))));
                desExc.execute(ExceptionReason.TOO_BIG_FILE_SIZE, () -> exceptionToClient.set(new StatusText("profile.too_big_file_size").convert(getLang(req))));
                desExc.execute(ExceptionReason.TOO_MANY_FILES, () -> exceptionToClient.set(new StatusText("profile.too_many_files").convert(getLang(req))));
            }

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            logger.error(exceptionToClient.get());

            sendException(exceptionToClient.get(), resp);
        }
    }
}
