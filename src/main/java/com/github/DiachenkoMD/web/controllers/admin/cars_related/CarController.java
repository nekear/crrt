package com.github.DiachenkoMD.web.controllers.admin.cars_related;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.enums.StatusStates;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.services.AdminService;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import static com.github.DiachenkoMD.web.utils.Utils.*;

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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@UseGuards({AuthGuard.class, AdminRGuard.class})
@WebServlet("/admin/car")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1 MB
        maxFileSize = 1024 * 1024 * 20,      // 20 MB
        maxRequestSize = 1024 * 1024 * 100  // 100 MB
)
public class CarController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(CarController.class);
    private AdminService adminService;
    private Gson gson;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        adminService = ((AdminService) config.getServletContext().getAttribute("admin_service"));
        gson = ((Gson) config.getServletContext().getAttribute("gson"));
    }

    /**
     * Route for getting detailed information about specific car.
     * @see AdminService#getCar(int)
     * @param req > "id": String
     * @param resp > {@link Car}
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String carIdString = req.getParameter("id");

            Car car = adminService.getCar(Integer.parseInt(CryptoStore.decrypt(carIdString)));

            sendSuccess(gson.toJson(car), resp);
        }catch (Exception e){
            logger.error(e);

            sendException(new StatusText("global.unexpectedError").convert(getLang(req)), resp);
        }
    }

    /**
     * Route for creating new car.
     * @see AdminService#createCar(HttpServletRequest)
     * @param req > Apart from n-number of images, should contain "document" field with JSON data. JSON is parsed with {@link Car} class, so it should contain all necessary fields.
     * @param resp > <code>{"carId": String, "message": String}</code>
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try{
            HashMap<String, String> carCreatingData = adminService.createCar(req);

            sendSuccess(gson.toJson(
                            Map.of(
                                    "carId", CryptoStore.encrypt(carCreatingData.get("id")),
                                    "message", new StatusText("admin.car_creation.successful", true, carCreatingData, StatusStates.SUCCESS).convert(getLang(req))
                            )
                        ), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            logger.error(e);

            if(e instanceof DescriptiveException desExc){
                desExc.execute(ExceptionReason.VALIDATION_ERROR, () -> exceptionToClient.set(new StatusText("admin.car_creation.validation_error").convert(getLang(req))));
                desExc.execute(ExceptionReason.BAD_VALUE, () -> exceptionToClient.set(new StatusText("admin.car_creation.low_price").convert(getLang(req))));
            }

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            logger.error(exceptionToClient.get());

            sendException(exceptionToClient.get(), resp);
        }
    }


    /**
     * Route for updating car`s data.
     * @see AdminService#updateCar(HttpServletRequest)
     * @param req > JSON is parsed with {@link Car} class, so it should contain all necessary fields.
     * @param resp > message
     */
    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) {
        try{
            adminService.updateCar(req);

            sendSuccess(new StatusText("admin.car_editing.update_successful").convert(getLang(req)), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            logger.error(e);

            if(e instanceof DescriptiveException desExc){
                desExc.execute(ExceptionReason.VALIDATION_ERROR, () -> exceptionToClient.set(new StatusText("admin.car_editing.validation_error").convert(getLang(req))));
            }

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            logger.error(exceptionToClient.get());

            sendException(exceptionToClient.get(), resp);
        }
    }

    /**
     * Route for deleting cars.
     * @see AdminService#deleteCar(HttpServletRequest)
     * @implNote If car has connected invoices, it won`t be deleted and controller will send error-message to client-side.
     * @param req > <code>{"id" (car id): String}</code>
     * @param resp > message
     */
    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try{
            adminService.deleteCar(req);

            sendSuccess(new StatusText("admin.car_editing.car_delete_successful").convert(getLang(req)), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            logger.error(e);

            if(e instanceof DescriptiveException desExc){
                desExc.execute(ExceptionReason.CAR_IN_USE, () -> exceptionToClient.set(new StatusText("admin.car_editing.car_in_use_error").convert(getLang(req))));
            }

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            logger.error(exceptionToClient.get());

            sendException(exceptionToClient.get(), resp);
        }
    }
}
