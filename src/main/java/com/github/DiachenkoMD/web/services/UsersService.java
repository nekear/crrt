package com.github.DiachenkoMD.web.services;

import static com.github.DiachenkoMD.entities.Constants.*;

import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.enums.ValidationParameters;
import com.github.DiachenkoMD.entities.enums.VisualThemes;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;

import static com.github.DiachenkoMD.web.utils.Utils.*;

import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.utils.RightsManager;
import com.github.DiachenkoMD.web.utils.Utils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsersService {

    private static final Logger logger = LogManager.getLogger(UsersService.class);
    private final UsersDAO usersDAO;

    public UsersService(UsersDAO usersDAO){
        this.usersDAO = usersDAO;
    }

    /**
     * Service method for registering new users. <br/>
     * Should have parameters like described below:
     * <ul>
     *     <li>{@link com.github.DiachenkoMD.entities.Constants#REQ_EMAIL REQ_EMAIL} (must-have)</li>
     *     <li>{@link com.github.DiachenkoMD.entities.Constants#REQ_FIRSTNAME REQ_FIRSTNAME}</li>
     *     <li>{@link com.github.DiachenkoMD.entities.Constants#REQ_SURNAME REQ_SURNAME}</li>
     *     <li>{@link com.github.DiachenkoMD.entities.Constants#REQ_PATRONYMIC REQ_PATRONYMIC}</li>
     *     <li>{@link com.github.DiachenkoMD.entities.Constants#REQ_PASSWORD REQ_PASSWORD} (must-have)</li>
     * </ul>
     * Incoming query should better contain unique (not already registered) email, otherwise this method will try to redirect to <i>/status</i> page and, additionally,
     * session`s attribute with <i>name="login_prg_message"</i> and <i>value="{some value from translation file}"</i>
     * @param req HttpServletRequest instance coming from controller
     * @param resp HttpServletResponse instance coming from controller
     */
    public String registerUser(HttpServletRequest req, HttpServletResponse resp) throws DescriptiveException, DBException{
        
        logger.debug("Method entered from {}", req.getRemoteAddr());

        // Gathering data from parameters
        String email = cleanGetString(req.getParameter(REQ_EMAIL));
        String firstname = cleanGetString(req.getParameter(REQ_FIRSTNAME));
        String surname = cleanGetString(req.getParameter(REQ_SURNAME));
        String patronymic = cleanGetString(req.getParameter(REQ_PATRONYMIC));
        String password = cleanGetString(req.getParameter(REQ_PASSWORD));

        logger.debug("Acquired values: {} {} {} {} {}", email ,firstname, surname, patronymic, password);

        // Validating data (email and password are always validated and firstname, surname and patronymic validate only if we got them from params)
        if (!validate(email, ValidationParameters.EMAIL))
            throw new DescriptiveException("Email validation failed", ExceptionReason.VALIDATION_ERROR);

        if (firstname != null && !validate(firstname, ValidationParameters.NAME))
            throw new DescriptiveException("Username validation failed", ExceptionReason.VALIDATION_ERROR);
        if (surname != null && !validate(surname, ValidationParameters.NAME))
            throw new DescriptiveException("Surname validation failed", ExceptionReason.VALIDATION_ERROR);
        if (patronymic != null && !validate(patronymic, ValidationParameters.NAME))
            throw new DescriptiveException("Patronymic validation failed", ExceptionReason.VALIDATION_ERROR);

        if (!validate(password, ValidationParameters.PASSWORD))
            throw new DescriptiveException("Password validation failed", ExceptionReason.VALIDATION_ERROR);

        // Creating User entity to send it to appropriate method later
        AuthUser registeringUser = AuthUser.of(email, firstname, surname, patronymic);

        // Checking user for existence
        boolean doesExist = usersDAO.doesExist(registeringUser);
        if (doesExist)
            throw new DescriptiveException(new HashMap<>(Map.of("email", email)), ExceptionReason.EMAIL_EXISTS);

        // Registering new user (method returns original user entity + newly created id included)
        AuthUser registeredUserEntity = usersDAO.completeRegister(registeringUser, encryptPassword(password));

        if(registeredUserEntity == null || registeredUserEntity.getId() == null)
            throw new DescriptiveException("Error while registering user", ExceptionReason.REGISTRATION_PROCESS_ERROR);

        String confirmationCode = registeredUserEntity.getConfirmationCode();

        emailNotify(registeringUser, "Account confirmation email from CRRT", "You can confirm your code at http://localhost:8080/crrt_war/confirmation?code="+confirmationCode);

        return email;
    }

    /**
     * Service method for confirmation of user`s email. <br/>
     * Should have parameter {@link com.github.DiachenkoMD.entities.Constants#REQ_CODE REQ_CODE}, otherwise will show error.
     * @param req HttpServletRequest instance coming from controller
     * @param resp HttpServletResponse instance coming from controller
     */
    public void confirmUserEmail(HttpServletRequest req, HttpServletResponse resp) throws DescriptiveException, DBException{
        String code = req.getParameter(REQ_CODE);

        if(code == null)
            throw new DescriptiveException(ExceptionReason.CONFIRMATION_CODE_EMPTY);

        AuthUser user = usersDAO.getUserByConfirmationCode(code);

        if(user == null)
            throw new DescriptiveException(ExceptionReason.CONFIRMATION_NO_SUCH_CODE);

        if(!usersDAO.setConfirmationCode(user.getCleanId().orElse(-1), null))
            throw new DescriptiveException(ExceptionReason.CONFIRMATION_PROCESS_ERROR);
    }

    /**
     * Service method for logging users into account. <br/>
     * Must contain {@link com.github.DiachenkoMD.entities.Constants#REQ_EMAIL REQ_EMAIL} and {@link com.github.DiachenkoMD.entities.Constants#REQ_PASSWORD REQ_PASSWORD} parameters, otherwise will send error response to user.
     * @param req HttpServletRequest instance coming from controller
     * @param resp HttpServletResponse instance coming from controller
     */
    public AuthUser loginUser(HttpServletRequest req, HttpServletResponse resp) throws DescriptiveException, IOException, DBException {

        String requestData = req.getReader().lines().collect(Collectors.joining());

        JSONObject acquiredData = new JSONObject(requestData);

        String email = acquiredData.getString(REQ_EMAIL);
        String password = acquiredData.getString(REQ_PASSWORD);

        logger.debug("Email: {} and password: {}", email, password);

        if(!validate(email, ValidationParameters.EMAIL))
            throw new DescriptiveException("Email validation failed", ExceptionReason.VALIDATION_ERROR);

        if(!validate(password, ValidationParameters.PASSWORD))
            throw new DescriptiveException("Password validation failed", ExceptionReason.VALIDATION_ERROR);

        AuthUser user = usersDAO.get(email);

        if(user == null)
            throw new DescriptiveException("User with such email was not found", ExceptionReason.LOGIN_USER_NOT_FOUND);

        if(user.getConfirmationCode() != null)
            throw new DescriptiveException("This account was not confirmed by email", ExceptionReason.LOGIN_NOT_CONFIRMED);

        String current_password = usersDAO.getPassword(user.getCleanId().get());

        if(!encryptedPasswordsCompare(password, current_password))
            throw new DescriptiveException("Password or email are invalid", ExceptionReason.LOGIN_WRONG_PASSWORD);

        // Removing user from rights manager queue, because there is no any sense to make another data update right after login (in fact, login already contains data reloading)
        ((RightsManager) req.getServletContext().getAttribute("rights_manager")).remove((Integer) user.getId());

        return user;
    }

    public AuthUser updateData(HttpServletRequest req, HttpServletResponse resp) throws DescriptiveException, IOException, DBException{
        String incomingJson = req.getReader().lines().collect(Collectors.joining("\n"));

        JSONObject json = new JSONObject(incomingJson);

        String firstname = json.has(REQ_FIRSTNAME) ? cleanGetString(json.getString(REQ_FIRSTNAME)) : null;
        String surname = json.has(REQ_SURNAME) ? cleanGetString(json.getString(REQ_SURNAME)) : null;
        String patronymic = json.has(REQ_PATRONYMIC) ? cleanGetString(json.getString(REQ_PATRONYMIC)) : null;

        HashMap<String, String> resultFieldsToUpdate = new HashMap<>();

        logger.debug("Acquired values: {} {} {}", firstname, surname, patronymic);

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

        if(resultFieldsToUpdate.size() == 0)
            throw new DescriptiveException(ExceptionReason.VALIDATION_ERROR);

        logger.debug("Going to update via HashMap: {}", resultFieldsToUpdate);

        AuthUser current = (AuthUser) req.getSession().getAttribute("auth");

        if(!usersDAO.updateUsersData(current.getCleanId().orElse(-1), resultFieldsToUpdate))
            throw new DescriptiveException(ExceptionReason.UUD_FAILED_TO_UPDATE);

        return usersDAO.get(current.getEmail());
    }

    public void updatePassword(HttpServletRequest req, HttpServletResponse resp) throws DescriptiveException, IOException, DBException{
        String incomingJson = req.getReader().lines().collect(Collectors.joining("\n"));

        JSONObject json = new JSONObject(incomingJson);

        String oldPassword =  cleanGetString(json.getString("old_password"));
        String newPassword =  cleanGetString(json.getString("new_password"));

        logger.debug("Acquired passwords: {} and {}", oldPassword, newPassword);

        if(!validate(newPassword, ValidationParameters.PASSWORD))
            throw new DescriptiveException("New password validation fail", ExceptionReason.VALIDATION_ERROR);

        AuthUser current = (AuthUser) req.getSession().getAttribute("auth");

        Integer user_id = current.getCleanId().orElseThrow(() -> new DescriptiveException("Unable to get user id from session", ExceptionReason.ACQUIRING_ERROR));

        String currentPassword = usersDAO.getPassword(user_id);

        if(!encryptedPasswordsCompare(oldPassword, currentPassword))
            throw new DescriptiveException("Old password and current one are not the same", ExceptionReason.UUD_PASSWORDS_DONT_MATCH);

        if(!usersDAO.setPassword(user_id, encryptPassword(newPassword)))
            throw new DescriptiveException("Zero rows returned from updating password in db", ExceptionReason.DB_ACTION_ERROR);
    }

    public double replenishBalance(HttpServletRequest req, HttpServletResponse resp) throws DescriptiveException, IOException, DBException{
        String incomingJson = req.getReader().lines().collect(Collectors.joining());

        JSONObject json = new JSONObject(incomingJson);

        double requestedAmount = json.getDouble("amount");

        logger.debug("Acquired amount {}", requestedAmount);

        if(requestedAmount <= 0)
            throw new DescriptiveException("Amount is less or equals 0", ExceptionReason.VALIDATION_ERROR);

        AuthUser current = (AuthUser) req.getSession().getAttribute("auth");

        Integer user_id = current.getCleanId().orElseThrow(() -> new DescriptiveException("Unable to get user id from session", ExceptionReason.ACQUIRING_ERROR));

        double currentBalance = usersDAO.getBalance(user_id);

        double newBalance = currentBalance + requestedAmount;

        if(!usersDAO.setBalance(user_id, newBalance))
            throw new DescriptiveException("Zero rows were updated (unable to update balance)", ExceptionReason.DB_ACTION_ERROR);

        current.setBalance(newBalance);

        return newBalance;
    }

    public String uploadAvatar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, DBException, DescriptiveException{
        Part filePart = req.getPart("avatar");

        if(req.getParts().size() > 1)
            throw new DescriptiveException("You can upload only one file as an avatar!", ExceptionReason.TOO_MANY_FILES);

        if(filePart.getSize() > 1024 * 1024 * 2) {
            throw new DescriptiveException("File size is too big!", ExceptionReason.TOO_BIG_FILE_SIZE);
        }

        if(!filePart.getSubmittedFileName().endsWith(".jpg") && !filePart.getSubmittedFileName().endsWith(".png"))
            throw new DescriptiveException("File should be .jpg or .png", ExceptionReason.BAD_FILE_EXTENSION);

        String realPath = req.getServletContext().getRealPath(AVATAR_UPLOAD_DIR);

        AuthUser currentUser = (AuthUser) req.getSession().getAttribute("auth");

        Integer user_id = currentUser.getCleanId().orElseThrow(() -> new DescriptiveException("Unable to get user id from session", ExceptionReason.ACQUIRING_ERROR));

        Optional<String> current_avatar = usersDAO.getAvatar(user_id);

        if(current_avatar.isPresent()) {
            Path avatarFilePath = Path.of(realPath, current_avatar.get());
            Files.delete(avatarFilePath);
            logger.debug("Deleting avatar with name [{}] from [{}]", current_avatar.get(), avatarFilePath);
        }

        String fileName = String.format("%s%s",System.currentTimeMillis(), random.nextInt(100000) + filePart.getSubmittedFileName());

        filePart.write(realPath + "/" + fileName);

        logger.debug("Uploading avatar with name [{}] to [{}]", fileName, realPath);

        if(!usersDAO.setAvatar(currentUser.getCleanId().orElse(-1), fileName))
            throw new DescriptiveException("Zero rows were updated while setting avatar in db", ExceptionReason.DB_ACTION_ERROR);


        currentUser.setAvatar(fileName);

        return fileName;

    }

    public String deleteAvatar(HttpServletRequest req, HttpServletResponse resp) throws DBException, DescriptiveException, IOException{
        String realPath = req.getServletContext().getRealPath("/uploads/avatars");

        AuthUser currentUser = (AuthUser) req.getSession().getAttribute("auth");

        Integer user_id = currentUser.getCleanId().orElseThrow(() -> new DescriptiveException("Unable to get user id from session", ExceptionReason.ACQUIRING_ERROR));

        Optional<String> current_avatar = usersDAO.getAvatar(user_id);

        if(current_avatar.isPresent()) {
            Path avatarFilePath = Path.of(realPath, current_avatar.get());
            Files.delete(avatarFilePath);
            logger.debug("Deleting avatar with name [{}] from [{}]", current_avatar.get(), avatarFilePath);

            if(!usersDAO.setAvatar(currentUser.getCleanId().orElse(-1), null))
                throw new DescriptiveException("Zero rows were updated while setting avatar in db", ExceptionReason.DB_ACTION_ERROR);

            currentUser.setAvatar(null);
        }

        return currentUser.idenAvatar(realPath);
    }

    public void changeTheme(HttpServletRequest req, HttpServletResponse resp){
        Cookie themeCookie = Utils.getCookieFromArray("theme", req.getCookies()).orElse(null);

        String path = req.getContextPath();

        if(themeCookie != null){
            if(VisualThemes.valueOf(themeCookie.getValue()) == VisualThemes.DARK){
                resp.addCookie(createCookie("theme", VisualThemes.LIGHT.toString(), path));
            }else{
                resp.addCookie(createCookie("theme", VisualThemes.DARK.toString(), path));
            }
        }else{
            resp.addCookie(createCookie("theme", VisualThemes.DARK.toString(), path));
        }
    }
}
