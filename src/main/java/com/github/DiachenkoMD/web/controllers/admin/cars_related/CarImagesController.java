package com.github.DiachenkoMD.web.controllers.admin.cars_related;

import com.github.DiachenkoMD.entities.dto.Image;
import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.enums.StatusStates;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.services.AdminService;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.roles.AdminRGuard;
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

import java.util.concurrent.atomic.AtomicReference;

import static com.github.DiachenkoMD.web.utils.Utils.*;

@UseGuards({AuthGuard.class, AdminRGuard.class})
@WebServlet("/admin/carImages")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1 MB
        maxFileSize = 1024 * 1024 * 20,      // 20 MB
        maxRequestSize = 1024 * 1024 * 100  // 100 MB
)
public class CarImagesController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(CarController.class);
    private AdminService adminService;
    private Gson gson;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        adminService = ((AdminService) config.getServletContext().getAttribute("admin_service"));
        gson = ((Gson) config.getServletContext().getAttribute("gson"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try{
            Image newImage = adminService.addImageToCar(req);

            resp.setContentType("application/json");
            sendSuccess(gson.toJson(newImage), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            logger.error(e);

            exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            logger.error(exceptionToClient.get());

            sendException(exceptionToClient.get(), resp);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try{
            adminService.deleteImageFromCar(req);

            sendSuccess(new StatusText("admin.car_editing.image_delete_successfully", true, StatusStates.SUCCESS).convert(getLang(req)), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            logger.error(e);

            if(e instanceof DescriptiveException desExc){
                desExc.execute(ExceptionReason.IMAGE_NOT_FOUND_IN_DB, () -> exceptionToClient.set(new StatusText("admin.car_editing.image_not_found_in_db").convert(getLang(req))));
                desExc.execute(ExceptionReason.DB_ACTION_ERROR, () -> exceptionToClient.set(new StatusText("admin.car_editing.no_entry_removed").convert(getLang(req))));
            }

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            logger.error(exceptionToClient.get());
        }
    }
}
