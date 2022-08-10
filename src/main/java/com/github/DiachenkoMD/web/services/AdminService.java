package com.github.DiachenkoMD.web.services;

import com.github.DiachenkoMD.entities.dto.*;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.google.gson.Gson;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.entities.Constants.IMAGES_UPLOAD_DIR;
import static com.github.DiachenkoMD.web.utils.Utils.random;

public class AdminService {

    private static final Logger logger = LogManager.getLogger(AdminService.class);
    private final UsersDAO usersDAO;
    private final CarsDAO carsDAO;
    private final InvoicesDAO invoicesDAO;

    private final ServletContext ctx;

    public AdminService(UsersDAO usersDAO, CarsDAO carsDAO, InvoicesDAO invoicesDAO, ServletContext ctx){
        this.usersDAO = usersDAO;
        this.carsDAO = carsDAO;
        this.invoicesDAO = invoicesDAO;
        this.ctx = ctx;
    }

    // TODO: add stats
    public List<Double> getStats(){
        return List.of(1000d, 1001d, 10d);
    }

    public List<Car> getCars() throws DBException {
        return carsDAO.getAll();
    }

    public Car getCar(int car_id) throws DBException, DescriptiveException {
        Optional<Car> car = carsDAO.get(car_id);

        return car.orElseThrow(() -> new DescriptiveException("Unable to get car", ExceptionReason.DB_ACTION_ERROR));
    }

    public HashMap<String, String> createCar(HttpServletRequest req) throws ServletException, IOException, DescriptiveException, DBException {

        // Acquiring gson
        Gson gson = (Gson) ctx.getAttribute("gson");

        // Parsing document object aka our car information (to upload files and json data together, i decided to add json as "document" parameter to sent files data)
        Part carInfoPart = req.getPart("document");
        String carInfoJson = new BufferedReader(new InputStreamReader(carInfoPart.getInputStream())).lines().collect(Collectors.joining());

        if(carInfoJson.isBlank())
            throw new DescriptiveException("Incoming json string is empty", ExceptionReason.VALIDATION_ERROR);

        Car creatingCar = null;
        try {
            creatingCar = gson.fromJson(carInfoJson, Car.class);
        }catch (NumberFormatException e){
            throw new DescriptiveException("Some fields are empty", ExceptionReason.VALIDATION_ERROR);
        }

        logger.debug("Incoming car for creation {}", creatingCar);

        // Validating car object
        if(creatingCar.getModel() == null || creatingCar.getBrand() == null || creatingCar.getPrice() == null || creatingCar.getSegment() == null || creatingCar.getCity() == null)
            throw new DescriptiveException("Car`s model / brand / price / segment / city mustn`t be null", ExceptionReason.VALIDATION_ERROR);

        if(creatingCar.getPrice() <= 0)
            throw new DescriptiveException("Car`s price should be positive number!", ExceptionReason.BAD_VALUE);

        // Processing incoming images
        Collection<Part> images = req.getParts();
        LinkedList<String> savedImages = new LinkedList<>();
        if(images.size() > 1){
            String realPath = req.getServletContext().getRealPath(IMAGES_UPLOAD_DIR);

            // Looping through all acquired from client
            try{
                for(Part filePart : images){
                    if(!filePart.getName().equals("document")){ // excluding "document" field as it`s not an image + we have already processed it
                        logger.debug("File: {} <-> {} <-> {}", filePart.getName(), filePart.getSubmittedFileName(), filePart.getContentType());

                        // Generating unique file name
                        String fileName = String.format("%s%s",System.currentTimeMillis(), random.nextInt(100000) + filePart.getSubmittedFileName());

                        logger.debug("Generated file name: " + fileName);

                        // Saving file to disk
                        filePart.write(realPath + "/" + fileName);

                        // Adding to uploaded images for 1) ability to remove all uploaded images if one failed 2) for batch inserting to db
                        savedImages.add(fileName);
                    }
                }
            }
            catch (IOException e){ // catching exceptions, occurred while saving files
                logger.error("Error while saving file to disk. Reverting all saved...");

                // Deleting all previously saved from disk
                for(String fileName : savedImages){
                    Path avatarFilePath = Path.of(realPath, fileName);
                    Files.delete(avatarFilePath);
                    logger.debug("Deleting avatar with name [{}] from [{}]", fileName, avatarFilePath);
                }

                throw e;
            }
        }

        // Creating new car entry in db
        int createdCarId = carsDAO.create(creatingCar);

        if(createdCarId == -1)
            throw new DescriptiveException("CarsDAO.create() returned -1 for some reason", ExceptionReason.DB_ACTION_ERROR);

        // Inserting images to db for created car
        carsDAO.addImages(createdCarId, savedImages);

        return new HashMap<>(
                Map.of(
                        "id", String.valueOf(createdCarId),
                        "brand", creatingCar.getBrand(),
                        "model", creatingCar.getModel()
                )
        );
    }

    public Image addImage(HttpServletRequest req) throws ServletException, IOException, DescriptiveException, DBException {

        // Parsing document object (to upload file and json data together, i decided to add json as "document" parameter to sent file data)
        Part jsonPart = req.getPart("document");
        String jsonDataStr = new BufferedReader(new InputStreamReader(jsonPart.getInputStream())).lines().collect(Collectors.joining());

        if(jsonDataStr.isBlank())
            throw new DescriptiveException("Incoming json string is empty", ExceptionReason.VALIDATION_ERROR);

        // Getting car id
        String carIdEncrypted = new JSONObject(jsonDataStr).getString("car_id");

        if(carIdEncrypted.isBlank())
            throw new DescriptiveException("Incoming car id is null", ExceptionReason.ACQUIRING_ERROR);

        int car_id = Integer.parseInt(CryptoStore.decrypt(carIdEncrypted));

        // Saving image to disk
        Part image = req.getPart("car-image");

        String realPath = ctx.getRealPath(IMAGES_UPLOAD_DIR);

        String fileName = String.format("%s%s",System.currentTimeMillis(), random.nextInt(100000) + image.getSubmittedFileName());

        image.write(realPath + "/" + fileName);

        logger.debug("Uploading car photo with name [{}] to [{}]", fileName, realPath);

        // Inserting image data to db
        int newImageId = carsDAO.addImage(car_id, fileName);
        if(newImageId < 0)
            throw new DescriptiveException("Zero rows inserted into the db", ExceptionReason.DB_ACTION_ERROR);

        Image responseImage = new Image();
        responseImage.setId(newImageId);
        responseImage.setFileName(fileName);

        return responseImage;
    }

    public void deleteImage(HttpServletRequest req) throws ServletException, IOException, DescriptiveException, DBException {
        // Acquiring image id (to know what image should be deleted)
        String jsonDataStr = new BufferedReader(req.getReader()).lines().collect(Collectors.joining());

        if(jsonDataStr.isBlank())
            throw new DescriptiveException("Incoming json string is empty", ExceptionReason.VALIDATION_ERROR);

        String carImageIdEncrypted = new JSONObject(jsonDataStr).getString("id");

        if(carImageIdEncrypted.isBlank())
            throw new DescriptiveException("Incoming image id is null", ExceptionReason.ACQUIRING_ERROR);

        int carImageId = Integer.parseInt(CryptoStore.decrypt(carImageIdEncrypted));

        // Getting image data from db (to know the file`s name)
        Image image = carsDAO.getImage(carImageId).orElseThrow(() -> new DescriptiveException("Image not found in db", ExceptionReason.IMAGE_NOT_FOUND_IN_DB));

        // Deleting file
        String realPath = ctx.getRealPath(IMAGES_UPLOAD_DIR);

        Path imagePath = Path.of(realPath, image.getFileName());

        Files.delete(imagePath);

        logger.debug("Deleting car image with name [{}] from [{}]",image.getFileName(), imagePath);

        // Removing file entry from db
        if(!carsDAO.deleteImage(carImageId))
            throw new DescriptiveException("Nothing was delete from db", ExceptionReason.DB_ACTION_ERROR);
    }

    public void updateCar(HttpServletRequest req) throws ServletException, IOException, DescriptiveException, DBException {

        // Acquiring gson
        Gson gson = (Gson) ctx.getAttribute("gson");

        // Parsing incoming car data
        String carInfoJson = new BufferedReader(req.getReader()).lines().collect(Collectors.joining());

        if(carInfoJson.isBlank())
            throw new DescriptiveException("Incoming json string is empty", ExceptionReason.VALIDATION_ERROR);

        Car car = null;
        try {
            car = gson.fromJson(carInfoJson, Car.class);
        }catch (NumberFormatException e){
            throw new DescriptiveException("Some fields are empty", ExceptionReason.VALIDATION_ERROR);
        }

        logger.debug("Incoming car for update {}", car);

        // Updating car`s db entry
        if(!carsDAO.update(car))
            throw new DescriptiveException("Failed to update db entry", ExceptionReason.DB_ACTION_ERROR);
    }

    public void deleteCar(HttpServletRequest req) throws IOException, DescriptiveException, DBException {
        // Acquiring car id
        String jsonDataStr = new BufferedReader(req.getReader()).lines().collect(Collectors.joining());

        String carIdEncrypted = new JSONObject(jsonDataStr).getString("id");

        int carId = Integer.parseInt(CryptoStore.decrypt(carIdEncrypted));

        HashMap<Integer, String> invoicesToClients = invoicesDAO.getOnCar(carId);

        if(invoicesToClients.size() > 0)
            throw new DescriptiveException("Unable to delete, because some clients have invoice on this car", ExceptionReason.CAR_IN_USE);

        // Getting connected images (from the Car object to let us send pretty message that <brand> <model> was successfully deleted or something like that)
        Car car = carsDAO.get(carId).get();

        if(car.getImages().size() > 0){
            String realPath = ctx.getRealPath(IMAGES_UPLOAD_DIR);
            for(Image image : car.getImages()){
                Path imagePath = Path.of(realPath, image.getFileName());

                Files.delete(imagePath);
                logger.debug("Deleting car image with name [{}] from [{}]",image.getFileName(), imagePath);
            }
        }


        // Removing file entry from db
        if(!carsDAO.delete(carId))
            throw new DescriptiveException("Zero rows were affected on car deleting", ExceptionReason.DB_ACTION_ERROR);
    }

    /**
     * Returns list of {@link LimitedUser PanelUser} depending on incoming criteria. Simply put, criteria contains pagination data + filters, collected from page. <br>
     * @see com.github.DiachenkoMD.entities.dto.PaginationWrapper PaginationWrapper
     * @see com.github.DiachenkoMD.entities.dto.Filters
     * @param paginationWrapperJSON json string, that should have view like {"askedPage":1,"elementsPerPage":15,"filters":{"email":"","firstname":"","surname":"","patronymic":"","role":0,"state":0}}
     */
    public List<LimitedUser> getUsers(String paginationWrapperJSON) throws DBException {
        Gson gson = (Gson) ctx.getAttribute("gson");
        PaginationWrapper pw = gson.fromJson(paginationWrapperJSON, PaginationWrapper.class);

        int askedPage = pw.getAskedPage();
        int elementsPerPage = pw.getElementsPerPage();

        int limitEnd = askedPage * elementsPerPage;
        int limitStart = limitEnd - elementsPerPage;

        HashMap<String, String> searchCriteria = pw.getUsersFilters().getDBPresentation();

        return usersDAO.getUserWithFilters(searchCriteria, limitStart, limitEnd);
    }
}
