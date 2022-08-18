package com.github.DiachenkoMD.web.services;

import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.dto.*;
import com.github.DiachenkoMD.entities.dto.users.InformativeUser;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.dto.users.PanelUser;
import com.github.DiachenkoMD.entities.enums.AccountStates;
import com.github.DiachenkoMD.entities.enums.Roles;
import com.github.DiachenkoMD.entities.enums.ValidationParameters;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.github.DiachenkoMD.web.utils.Utils;
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

import static com.github.DiachenkoMD.entities.Constants.*;
import static com.github.DiachenkoMD.web.utils.Utils.*;

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

    /** Method for getting global stats. Just calls for {@link InvoicesDAO#getStats()}, so read about it {@link InvoicesDAO#getStats() here}.
     *
     * @return
     * @throws DBException
     */
    public List<Double> getStats() throws DBException {
        return invoicesDAO.getStats();
    }

    /**
     * Simple method for getting all cars info for reactive search at admin-panel. Inside just calls {@link CarsDAO#getAll()}.
     * @return List of {@link Car}
     * @throws DBException
     */
    public List<Car> getCars() throws DBException {
        return carsDAO.getAll();
    }

    /**
     * Method for getting car data for specific car.
     * @param car_id decrypted car id.
     * @return {@link Car} info.
     * @throws DBException comes from {@link CarsDAO#get(int)}.
     * @throws DescriptiveException rudimentary
     */
    public Car getCar(int car_id) throws DBException, DescriptiveException {
        Optional<Car> car = carsDAO.get(car_id);

        return car.orElseThrow(() -> new DescriptiveException("Unable to get car", ExceptionReason.DB_ACTION_ERROR));
    }

    /**
     * Service method for car creation. Awaits getting from req multipart data, where could be photos (if user decided to add them) and 100% "document" field with all car information.
     * @param req
     * @return HashMap with keys: <ul>
     *     <li>Newly created car id</li>
     *     <li>Car brand (for pretty message)</li>
     *     <li>Car model (for pretty message)</li>
     * </ul>
     * @throws ServletException
     * @throws IOException from {@link Part#getInputStream()}
     * @throws DescriptiveException with reasons {@link ExceptionReason#VALIDATION_ERROR VALIDATION_ERROR}, {@link ExceptionReason#BAD_VALUE BAD_VALUE} (if car`s price is negative number).
     * @throws DBException comes from {@link CarsDAO#create(Car)} and {@link CarsDAO#addImage(int, String)}
     */

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
                        String fileName = String.format("%s%s",System.currentTimeMillis(), random.nextInt(100000) + Utils.generateRandomString(6) + Utils.getFileExtension(filePart.getSubmittedFileName()));

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

    /**
     * Method for adding images to car. Accepts only 1 image. Talking about interesting moments, this method almost randomly generates file name and file input should have name "car-image".
     * @param req should contain image data + "document" parameter with encrypted "car_id" in json format.
     * @return {@link Image} object with newly created image id and its generated name.
     * @throws ServletException
     * @throws IOException from {@link Part#getInputStream()} to read json data from "document" field. (contains car_id field).
     * @throws DescriptiveException with reasons {@link ExceptionReason#VALIDATION_ERROR VALIDATION_ERROR} and {@link ExceptionReason#ACQUIRING_ERROR ACQUIRING_ERROR} (if incoming car id is null).
     * @throws DBException comes from {@link CarsDAO#addImage(int, String)}
     */
    public Image addImageToCar(HttpServletRequest req) throws ServletException, IOException, DescriptiveException, DBException {

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

        logger.debug("File: {} <-> {} <-> {}", image.getName(), jsonPart.getSubmittedFileName(), jsonPart.getContentType());

        String fileName = String.format("%s%s",System.currentTimeMillis(), random.nextInt(100000) + Utils.generateRandomString(6) + Utils.getFileExtension(image.getSubmittedFileName()));

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

    /**
     * Method for deleting images from car. Request object should contain "id" parameter. Generally, this method is well documented inside (as others).
     * @param req should contain "id" parameter.
     * @throws ServletException
     * @throws IOException
     * @throws DescriptiveException with reasons: <ul>
     *     <li>{@link ExceptionReason#VALIDATION_ERROR VALIDATION_ERROR} </li>
     *     <li>{@link ExceptionReason#ACQUIRING_ERROR ACQUIRING_ERROR} if incoming image id is null</li>
     *     <li>{@link ExceptionReason#IMAGE_NOT_FOUND_IN_DB IMAGE_NOT_FOUND_IN_DB} if incoming image id is null</li>
     *     </ul>
     * @throws DBException from {@link CarsDAO#getImage(int)} and {@link CarsDAO#deleteImage(int)}.
     */
    public void deleteImageFromCar(HttpServletRequest req) throws ServletException, IOException, DescriptiveException, DBException {
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

    /**
     * Method for updating car data. Doesn`t await any images, so accept only text data which is similar to {@link Car} class (because inside is being parsed by json with this class as prototype).
     * @param req should have content similar to {@link Car}.
     * @throws ServletException
     * @throws IOException
     * @throws DescriptiveException with reasons {@link ExceptionReason#VALIDATION_ERROR VALIDATION_ERROR}.
     * @throws DBException from {@link CarsDAO#update(Car)}.
     */
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

    /**
     * Method for deleting cars. In request body should contain encrypted car id.
     * @param req should contain encrypted car id.
     * @throws IOException
     * @throws DescriptiveException with reasons {@link ExceptionReason#CAR_IN_USE CAR_IN_USE} (if car has connected invoices)
     * @throws DBException comes from {@link InvoicesDAO#getInvoicesOnCar(int)} (for getting connected Invoices and clients) and {@link CarsDAO#delete(int)}.
     */
    public void deleteCar(HttpServletRequest req) throws IOException, DescriptiveException, DBException {
        // Acquiring car id
        String jsonDataStr = new BufferedReader(req.getReader()).lines().collect(Collectors.joining());

        String carIdEncrypted = new JSONObject(jsonDataStr).getString("id");

        int carId = Integer.parseInt(CryptoStore.decrypt(carIdEncrypted));

        HashMap<Integer, String> invoicesToClients = invoicesDAO.getInvoicesOnCar(carId);

        if(invoicesToClients.size() > 0)
            throw new DescriptiveException("Unable to delete, because some clients have invoice on this car", ExceptionReason.CAR_IN_USE);

        // Getting connected images (from the Car object to let us send pretty message that <brand> <model> was successfully deleted or something like that)
        Car car = carsDAO.get(carId).get(); // TODO:: return brand and model to pretty show success

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
     * Returns list of {@link PanelUser PanelUser} depending on incoming criteria. Simply put, criteria contains pagination data + filters, collected from page. <br>
     * @see PaginationRequest PaginationWrapper
     * @see com.github.DiachenkoMD.entities.dto.Filters
     * @param paginationRequestJSON json string, that should have view like {"askedPage":1,"elementsPerPage":15,"filters":{"email":"","firstname":"","surname":"","patronymic":"","role":0,"state":0}}
     */
    public PaginationResponse<PanelUser> getUsers(String paginationRequestJSON) throws DBException {
        Gson gson = (Gson) ctx.getAttribute("gson");
        PaginationRequest pr = gson.fromJson(paginationRequestJSON, PaginationRequest.class);

        int askedPage = pr.getAskedPage();
        int elementsPerPage = pr.getElementsPerPage();

        int limitOffset = (askedPage - 1) * elementsPerPage;
        int limitCount = elementsPerPage;

        HashMap<String, String> searchCriteria = pr.getUsersFilters().getDBPresentation("%");

        List<PanelUser> foundUsersWithLimit = usersDAO.getUsersWithFilters(searchCriteria, limitOffset, limitCount);
        int totalUsersAmount = usersDAO.getUsersNumberWithFilters(searchCriteria);

        PaginationResponse<PanelUser> paginationResponse = new PaginationResponse<>();
        paginationResponse.setTotalElements(totalUsersAmount);
        paginationResponse.setResponseData(foundUsersWithLimit);

        return paginationResponse;
    }

    /**
     * Method for creating new user from admin-panel. Contains email, firstname?, surname?, patronymic?, password and role validation, so be sure to pass in JSON all needed data.
     * @param creationUserJSON - json string containing email, firstname?, surname?, patronymic?, password and role. Note: "?" means optional.
     * @throws DBException depending on two calls of {@link UsersDAO#doesExist(LimitedUser)} and {@link UsersDAO#register(LimitedUser, String)}
     * @throws DescriptiveException with reasons {@link ExceptionReason#VALIDATION_ERROR VALIDATION_ERROR} and {@link ExceptionReason#EMAIL_EXISTS EMAIL_EXISTS}
     */
    public void createUser(String creationUserJSON) throws DBException, DescriptiveException{
        Gson gson = (Gson) ctx.getAttribute("gson");

        CreationUpdatingUserJPC registeringUser = gson.fromJson(creationUserJSON, CreationUpdatingUserJPC.class);

        String email = registeringUser.getEmail();
        String firstname = registeringUser.getFirstname();
        String surname = registeringUser.getSurname();
        String patronymic = registeringUser.getPatronymic();
        String password = registeringUser.getPassword();
        Roles role = registeringUser.getRole();


        logger.debug("Acquired values: {} {} {} {} {}, {}", email ,firstname, surname, patronymic, password, role);

        // Validating data (email and password are always validated and firstname, surname and patronymic validate only if we got them from json object)
        if (!validate(email, ValidationParameters.EMAIL))
            throw new DescriptiveException("Email validation failed", ExceptionReason.VALIDATION_ERROR);

        if (firstname != null && !validate(firstname, ValidationParameters.NAME))
            throw new DescriptiveException("Firstname validation failed", ExceptionReason.VALIDATION_ERROR);
        if (surname != null && !validate(surname, ValidationParameters.NAME))
            throw new DescriptiveException("Surname validation failed", ExceptionReason.VALIDATION_ERROR);
        if (patronymic != null && !validate(patronymic, ValidationParameters.NAME))
            throw new DescriptiveException("Patronymic validation failed", ExceptionReason.VALIDATION_ERROR);

        if (!validate(password, ValidationParameters.PASSWORD))
            throw new DescriptiveException("Password validation failed", ExceptionReason.VALIDATION_ERROR);

        if (role == null)
            throw new DescriptiveException("Role validation failed", ExceptionReason.VALIDATION_ERROR);

        // Checking user for existence
        boolean doesExist = usersDAO.doesExist(registeringUser);
        if (doesExist)
            throw new DescriptiveException(new HashMap<>(Map.of("email", email)), ExceptionReason.EMAIL_EXISTS);

        // Registering new user (method returns original user entity + newly created id included)
        if(usersDAO.register(registeringUser, encryptPassword(password)) == null)
            throw new DescriptiveException("Got null from calling register() method", ExceptionReason.DB_ACTION_ERROR);
    }

    /**
     * JSON Parsing Class (JPC) for GSON. Created for parsing incoming json data for user creation at {@link #createUser(String) createUser(String)}. <br/>
     * Extends LimitedUser only providing new field for password.
     */
    private static class CreationUpdatingUserJPC extends LimitedUser {
        private String password;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * Method for getting extended user information for admin-panel.
     * @param userIdEncrypted User identifier in the encrypted state.
     * @throws DescriptiveException comes from call to {@link CryptoStore#decrypt(String)}
     * @throws DBException depending on call to {@link UsersDAO#getInformativeUser(int)}
     */
    public InformativeUser getUser(String userIdEncrypted) throws DescriptiveException, DBException {
        int userId = Integer.parseInt(CryptoStore.decrypt(userIdEncrypted));

        InformativeUser user = usersDAO.getInformativeUser(userId);

        if(user == null)
            throw new DescriptiveException("getInformativeUser return null (no user was acquired)", ExceptionReason.DB_ACTION_ERROR);

        return user;
    }

    /**
     * Method for updating user data. Obtaining !only! changed fields, any other field, that is not on json object won`t be updated.
     * @param changedUserDataJSON changed user data in json format. Should correspond to {@link CreationUpdatingUserJPC CreationUpdatingUserJPC}, because will be parsed by Gson into that object.
     * @throws DescriptiveException with reasons {@link ExceptionReason#ACQUIRING_ERROR ACQUIRING_ERROR} (if could not obtain user id from request) and {@link ExceptionReason#VALIDATION_ERROR VALIDATION_ERROR}.
     * @throws DBException comes from {@link UsersDAO#updateUsersData(int, HashMap)}.
     */
    public void updateUser(String changedUserDataJSON) throws DescriptiveException, DBException {
        Gson gson = (Gson) ctx.getAttribute("gson");

        CreationUpdatingUserJPC changedUserData = gson.fromJson(changedUserDataJSON, CreationUpdatingUserJPC.class);

        String email = changedUserData.getEmail();
        String firstname = changedUserData.getFirstname();
        String surname = changedUserData.getSurname();
        String patronymic = changedUserData.getPatronymic();
        String password = changedUserData.getPassword();
        Roles role = changedUserData.getRole();


        logger.debug("Acquired values: {} {} {} {} {}, {}", email ,firstname, surname, patronymic, password, role);

        HashMap<String, String> resultFieldsToUpdate = new HashMap<>();

        int userId = changedUserData.getCleanId().orElseThrow(() -> new DescriptiveException("Could not obtain user id from request json object", ExceptionReason.ACQUIRING_ERROR));

        // Validating data
        if (email != null){
            if(!validate(email, ValidationParameters.EMAIL))
                throw new DescriptiveException("Email validation failed", ExceptionReason.VALIDATION_ERROR);
            resultFieldsToUpdate.put(DB_Constants.TBL_USERS_EMAIL, email);
        }

        if(firstname != null){
            if(!validate(firstname, ValidationParameters.NAME))
                throw new DescriptiveException("Firstname validation failed", ExceptionReason.VALIDATION_ERROR);
            resultFieldsToUpdate.put(DB_Constants.TBL_USERS_FIRSTNAME, firstname);
        }

        if(surname != null){
            if(!validate(surname, ValidationParameters.NAME))
                throw new DescriptiveException("Surname validation failed", ExceptionReason.VALIDATION_ERROR);
            resultFieldsToUpdate.put(DB_Constants.TBL_USERS_SURNAME, surname);
        }

        if(patronymic != null){
            if(!validate(patronymic, ValidationParameters.NAME))
                throw new DescriptiveException("Patronymic validation failed", ExceptionReason.VALIDATION_ERROR);
            resultFieldsToUpdate.put(DB_Constants.TBL_USERS_PATRONYMIC, patronymic);
        }

        if (password != null){
            if(!validate(password, ValidationParameters.PASSWORD))
                throw new DescriptiveException("Password validation failed", ExceptionReason.VALIDATION_ERROR);
            resultFieldsToUpdate.put(DB_Constants.TBL_USERS_PASSWORD, encryptPassword(password));
        }

        if (role != null)
            resultFieldsToUpdate.put(DB_Constants.TBL_USERS_ROLE_ID, String.valueOf(role.id()));


        if(resultFieldsToUpdate.size() > 0)
            if(!usersDAO.updateUsersData(userId, resultFieldsToUpdate))
                throw new DescriptiveException("Zero rows were updated by calling updateUserData", ExceptionReason.DB_ACTION_ERROR);
    }

    /**
     * Method for updating user state aka blocking / unblocking user.
     * @param userIdEncrypted encrypted user id as String.
     * @param newStateId id of state as int (by 2022.08.11 -> 1 = blocked, 2 = unblocked).
     * @throws DescriptiveException throws (not) stupid {@link ExceptionReason#DB_ACTION_ERROR DB_ACTION_ERROR} which never ever will be thrown.
     * @throws DBException comes from {@link UsersDAO#setUserState(int, int)}
     */
    public void updateUserState(String userIdEncrypted, int newStateId) throws DescriptiveException, DBException {

        int userId = Integer.parseInt(CryptoStore.decrypt(userIdEncrypted));

        AccountStates newState = AccountStates.getById(newStateId);

        if(!usersDAO.setUserState(userId, newState.id()))
            throw new DescriptiveException("Zero rows were updated by calling setUserState()", ExceptionReason.DB_ACTION_ERROR);
    }

    /**
     * Method for deleting users. Accepting array of users` ids in encrypted state.
     * @param usersListJSON json object containing inside a list of users` ids in encrypted state.
     * @throws DBException comes from {@link UsersDAO#deleteUsers(List)}
     * @throws DescriptiveException
     */
    public void deleteUsers(String usersListJSON) throws DBException, DescriptiveException {
        Gson gson = (Gson) ctx.getAttribute("gson");
        DeleteUsersJPC deleteUsersData = gson.fromJson(usersListJSON, DeleteUsersJPC.class);


        List<Integer> usersIds = deleteUsersData.getIds().parallelStream().map(x -> {
            try {
                return Integer.parseInt(CryptoStore.decrypt(x));
            } catch (DescriptiveException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        if(usersIds.size() > 0){
            if(!usersDAO.deleteUsers(usersIds))
                throw new DescriptiveException("Some or all users were not deleted!", ExceptionReason.DB_ACTION_ERROR);
        }
    }

    private static class DeleteUsersJPC{

        private List<String> ids;

        public List<String> getIds() {
            return ids;
        }

        public void setIds(List<String> ids) {
            this.ids = ids;
        }
    }
}
