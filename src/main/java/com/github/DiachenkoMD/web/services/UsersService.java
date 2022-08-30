package com.github.DiachenkoMD.web.services;

import static com.github.DiachenkoMD.entities.Constants.*;

import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.dto.JWTAnalysis;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.enums.ValidationParameters;
import com.github.DiachenkoMD.entities.enums.VisualThemes;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;

import static com.github.DiachenkoMD.web.utils.Utils.*;

import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.utils.*;
import io.fusionauth.jwt.domain.JWT;
import jakarta.servlet.Servlet;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsersService {

    private static final Logger logger = LogManager.getLogger(UsersService.class);
    private final UsersDAO usersDAO;
    private final ServletContext ctx;

    public UsersService(UsersDAO usersDAO, ServletContext ctx){
        this.usersDAO = usersDAO;
        this.ctx = ctx;
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
    public String registerUser(HttpServletRequest req, HttpServletResponse resp) throws DescriptiveException, DBException, IOException {
        
        logger.debug("Method entered from {}", req.getRemoteAddr());

        // Verifying recaptcha
        String recaptchaResponse = cleanGetString(req.getParameter("g-recaptcha-response"));
        boolean isRecaptchaVerified = RecaptchaVerifier.verify(recaptchaResponse);

        if(!isRecaptchaVerified)
            throw new DescriptiveException("Recaptcha is not verified!", ExceptionReason.RECAPTCHA_VERIFICATION_ERROR);

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

        emailNotify(registeringUser, "Account confirmation email from CRRT", "You can confirm your account by clicking on <a href='http://localhost:8080/crrt_war/confirmation?code="+confirmationCode+"'>this</a> link.");

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
    public Map.Entry<AuthUser, Boolean> loginUser(HttpServletRequest req, HttpServletResponse resp) throws DescriptiveException, IOException, DBException {
        // Getting json data
        String requestData = req.getReader().lines().collect(Collectors.joining());

        JSONObject acquiredData = new JSONObject(requestData);

        String email = acquiredData.getString(REQ_EMAIL);
        String password = acquiredData.getString(REQ_PASSWORD);
        boolean shouldRemember = acquiredData.getBoolean(REQ_SHOULD_REMEMBER);

        logger.debug("Email: {} and password: {}", email, password);

        // Validating email and password
        if(!validate(email, ValidationParameters.EMAIL))
            throw new DescriptiveException("Email validation failed", ExceptionReason.VALIDATION_ERROR);

        if(!validate(password, ValidationParameters.PASSWORD))
            throw new DescriptiveException("Password validation failed", ExceptionReason.VALIDATION_ERROR);

        // Executing general checks for user
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

        return Map.entry(user, shouldRemember);
    }

    /**
     * Method for updating user data on profile page.
     * @param req should contain json object with optional fields like "firstname", "surname" and "patronymic".
     * @param resp
     * @return updated user data as {@link AuthUser} object.
     * @throws DescriptiveException might be thrown with {@link ExceptionReason#VALIDATION_ERROR VALIDATION_ERROR} reason.
     * @throws IOException might be thrown from {@link HttpServletRequest#getReader() getReader()} method.
     * @throws DBException might be thrown from {@link UsersDAO#updateUsersData(int, HashMap) updateUsersData(int, HashMap)} or {@link UsersDAO#get(String) get(String)}.
     */
    public AuthUser updateData(HttpServletRequest req, HttpServletResponse resp) throws DescriptiveException, IOException, DBException{
        // Getting incoming json data
        String incomingJson = req.getReader().lines().collect(Collectors.joining("\n"));

        JSONObject json = new JSONObject(incomingJson);

        String firstname = json.has(REQ_FIRSTNAME) ? cleanGetString(json.getString(REQ_FIRSTNAME)) : null;
        String surname = json.has(REQ_SURNAME) ? cleanGetString(json.getString(REQ_SURNAME)) : null;
        String patronymic = json.has(REQ_PATRONYMIC) ? cleanGetString(json.getString(REQ_PATRONYMIC)) : null;

        HashMap<String, String> resultFieldsToUpdate = new HashMap<>();

        logger.debug("Acquired values: {} {} {}", firstname, surname, patronymic);

        // Validating firstname, surname and patronymic (if any is not null)
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

        logger.debug("Going to update user data via HashMap: {}", resultFieldsToUpdate);

        // Upadting user data and returning updated data from db
        AuthUser current = (AuthUser) req.getSession().getAttribute(SESSION_AUTH);

        int userId = (Integer) current.getId();

        usersDAO.updateUsersData(userId, resultFieldsToUpdate);

        return usersDAO.get(userId);
    }

    /**
     * Method for updating user`s password. Used from profile page.
     * @param req should contain json object with fields "old_password" and "new_password".
     * @param resp
     * @throws DescriptiveException might be thrown with many different reasons like:
     * <ul>
     *     <li>VALIDATION_ERROR</li>
     *     <li>SESSION_AUTH</li>
     *     <li>UUD_PASSWORDS_DONT_MATCH</li>
     *     <li>DB_ACTION_ERROR (if password was not updated)</li>
     * </ul>
     * @throws DBException might be thrown from {@link UsersDAO#getPassword(int)} and {@link UsersDAO#setPassword(int, String)}
     */
    public void updatePassword(HttpServletRequest req, HttpServletResponse resp) throws DescriptiveException, IOException, DBException{
        // Getting incoming json data and extracting "old_password" and "new_password"
        String incomingJson = req.getReader().lines().collect(Collectors.joining("\n"));

        JSONObject json = new JSONObject(incomingJson);

        String oldPassword =  cleanGetString(json.getString("old_password"));
        String newPassword =  cleanGetString(json.getString("new_password"));

        logger.debug("Acquired passwords: {} and {}", oldPassword, newPassword);

        // Validating new password
        if(!validate(newPassword, ValidationParameters.PASSWORD))
            throw new DescriptiveException("New password validation fail", ExceptionReason.VALIDATION_ERROR);

        // Getting user from session
        AuthUser current = (AuthUser) req.getSession().getAttribute(SESSION_AUTH);

        int userId = (Integer) current.getId();

        // Getting current password to compare it further
        String currentPassword = usersDAO.getPassword(userId);

        // Comparing passwords
        if(!encryptedPasswordsCompare(oldPassword, currentPassword))
            throw new DescriptiveException("Old password and current one are not the same", ExceptionReason.UUD_PASSWORDS_DONT_MATCH);

        // Updating user password
        if(!usersDAO.setPassword(userId, encryptPassword(newPassword)))
            throw new DescriptiveException("Zero rows returned from updating password in db", ExceptionReason.DB_ACTION_ERROR);
    }

    /**
     * Method for replenishing user`s balance.
     * @param req should contain json object with "amount" field inside.
     * @param resp
     * @return new user`s balance
     * @throws DescriptiveException might be thrown with reasons VALIDATION_ERROR, ACQUIRING_ERROR, DB_ACTION_ERROR.
     * @throws DBException might be thrown from {@link UsersDAO#getBalance(int)} and {@link UsersDAO#setBalance(int, double)}
     */
    public double replenishBalance(HttpServletRequest req, HttpServletResponse resp) throws DescriptiveException, IOException, DBException{
        // Getting incoming json and extracting "amount"
        String incomingJson = req.getReader().lines().collect(Collectors.joining());

        JSONObject json = new JSONObject(incomingJson);

        double requestedAmount = json.getDouble("amount");

        logger.debug("Acquired amount {}", requestedAmount);

        // Validating money amount (should be > 0)
        if(requestedAmount <= 0)
            throw new DescriptiveException("Amount is less or equals 0", ExceptionReason.VALIDATION_ERROR);

        // Gathering user data
        AuthUser current = (AuthUser) req.getSession().getAttribute(SESSION_AUTH);
        int userId = (Integer) current.getId();
        double currentBalance = usersDAO.getBalance(userId);

        // Calculating new balance and updating it
        double newBalance = currentBalance + requestedAmount;

        if(!usersDAO.setBalance(userId, newBalance))
            throw new DescriptiveException("Zero rows were updated (unable to update balance)", ExceptionReason.DB_ACTION_ERROR);

        current.setBalance(newBalance);

        return newBalance;
    }

    /**
     * Method for uploading user`s avatar.
     * @param req should contain {@link Part} with "avatar" field.
     * @param resp
     * @return newly generated avatar file name.
     * @throws DBException may be thrown from {@link UsersDAO#getAvatar(int)}, {@link UsersDAO#setAvatar(int, String)}.
     * @throws DescriptiveException might be thrown with reasons TOO_MANY_FILES, TOO_BIG_FILE_SIZE, BAD_FILE_EXTENSION, ACQUIRING_ERROR, DB_ACTION_ERROR.
     */
    public String uploadAvatar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, DBException, DescriptiveException{
        // Getting avatar file part and executing some checks for size, amount of files acquired (should be == 1) and extension of file.
        Part filePart = req.getPart("avatar");

        if(req.getParts().size() > 1)
            throw new DescriptiveException("You can upload only one file as an avatar!", ExceptionReason.TOO_MANY_FILES);

        if(filePart.getSize() > 1024 * 1024 * 2) {
            throw new DescriptiveException("File size is too big!", ExceptionReason.TOO_BIG_FILE_SIZE);
        }

        if(!filePart.getSubmittedFileName().endsWith(".jpg") && !filePart.getSubmittedFileName().endsWith(".png"))
            throw new DescriptiveException("File should be .jpg or .png", ExceptionReason.BAD_FILE_EXTENSION);

        String realPath = req.getServletContext().getRealPath(AVATAR_UPLOAD_DIR);

        AuthUser currentUser = (AuthUser) req.getSession().getAttribute(SESSION_AUTH);

        int userId = (Integer) currentUser.getId();

        Optional<String> current_avatar = usersDAO.getAvatar(userId);

        // Deleting previous avatar if has been set before
        if(current_avatar.isPresent()) {
            Path avatarFilePath = Path.of(realPath, current_avatar.get());
            Files.delete(avatarFilePath);
            logger.debug("Deleting avatar with name [{}] from [{}]", current_avatar.get(), avatarFilePath);
        }

        // Generating new file name and saving avatar
        String fileName = String.format("%s%s",System.currentTimeMillis(), random.nextInt(100000) + filePart.getSubmittedFileName());

        filePart.write(realPath + "/" + fileName);

        logger.debug("Uploading avatar with name [{}] to [{}]", fileName, realPath);

        if(!usersDAO.setAvatar((Integer) currentUser.getId(), fileName))
            throw new DescriptiveException("Zero rows were updated while setting avatar in db", ExceptionReason.DB_ACTION_ERROR);


        currentUser.setAvatar(fileName);

        return fileName;

    }

    /**
     * Method for deleting user avatar
     * @param req awaits nothing.
     * @param resp
     * @return name of new avatar (basically, it is generated from current user login)
     * @throws DBException may be thrown from {@link UsersDAO#getAvatar(int)} and {@link UsersDAO#setAvatar(int, String)}.
     * @throws DescriptiveException may be thrown with reason DB_ACTION_ERROR.
     */
    public String deleteAvatar(HttpServletRequest req, HttpServletResponse resp) throws DBException, DescriptiveException, IOException{
        String realPath = req.getServletContext().getRealPath("/uploads/avatars");

        AuthUser currentUser = (AuthUser) req.getSession().getAttribute(SESSION_AUTH);

        int userId = (Integer) currentUser.getId();

        // If user have avatar, we should delete it otherwise no actions should be done
        Optional<String> currentAvatar = usersDAO.getAvatar(userId);
        if(currentAvatar.isPresent()) {
            Path avatarFilePath = Path.of(realPath, currentAvatar.get());
            Files.delete(avatarFilePath);
            logger.debug("Deleting avatar with name [{}] from [{}]", currentAvatar.get(), avatarFilePath);

            if(!usersDAO.setAvatar(userId, null))
                throw new DescriptiveException("Zero rows were updated while setting avatar in db", ExceptionReason.DB_ACTION_ERROR);

            currentUser.setAvatar(null);
        }

        return currentUser.idenAvatar(realPath);
    }

    /**
     * Method for changing current visual theme.
     * @param req should better contain "theme" cookie otherwise theme will be set to dark.
     * @param resp
     */
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

    /**
     * Method for generating unique restoration JWT token and sending email with it to user.
     * @param email where the email with restoration link should be sent.
     * @throws DBException might be thrown from {@link UsersDAO#doesExist(LimitedUser)}.
     * @throws DescriptiveException might be thrown with reason ACQUIRING_ERROR.
     */
    public void sendRestorationLink(String email) throws DBException, DescriptiveException {
        if(!usersDAO.doesExist(email))
            throw new DescriptiveException("Could find user with entered email " + email, ExceptionReason.ACQUIRING_ERROR);

        LocalDateTime expirationDate = LocalDateTime.now().plusDays(1);

        String restorationToken = JWTManager.encode("email", email.trim(), expirationDate);

        emailNotify(email,
                "Account password recovery",
                String.format(
                        "Good afternoon. To reset your account password, follow this <a href='http://localhost:8080%s/restore?token=%s'>link</a>. <p>This link is available till %s.<p/>",
                        ctx.getContextPath(),
                        restorationToken,
                        expirationDate.format(localDateTimeFormatter)
                )
        );
    }

    /**
     * Method for changing user password on "password restore" operation.
     * @param jwtToken token, which should contain user email
     * @param password new user password
     * @throws DescriptiveException may be thrown with reasons
     * {@link ExceptionReason#ACQUIRING_ERROR ACQUIRING_ERROR},
     * {@link ExceptionReason#VALIDATION_ERROR VALIDATION_ERROR},
     * {@link ExceptionReason#TOKEN_ALREADY_USED TOKEN_ALREADY_USED},
     * {@link ExceptionReason#TOKEN_ALREADY_EXPIRED TOKEN_ALREADY_EXPIRED}
     * @throws DBException may be thrown from methods {@link UsersDAO#get(String)} and {@link UsersDAO#setPassword(int, String)}.
     */
    public void updatePasswordForAccount(String jwtToken, String password) throws DescriptiveException, DBException {
        // Validating token
        JWTAnalysis jwtAnalysis = JWTAnalysis.of(jwtToken);

        if(jwtAnalysis.isDisabled())
            throw new DescriptiveException("Token has been already used", ExceptionReason.TOKEN_ALREADY_USED);

        if(jwtAnalysis.isExpired())
            throw new DescriptiveException("Token has been already expired", ExceptionReason.TOKEN_ALREADY_EXPIRED);

        if(!jwtAnalysis.isValid() || !jwtAnalysis.containsFields("email"))
            throw new DescriptiveException("Enable to validate jwt token", ExceptionReason.ACQUIRING_ERROR);

        // Validating password
        if(!Validatable.of(password, ValidationParameters.PASSWORD, false).validate())
            throw new DescriptiveException("Password failed validation", ExceptionReason.VALIDATION_ERROR);

        JWT jwt = jwtAnalysis.getToken();

        // Changing password
        String email = jwt.getString("email");
        LimitedUser user = usersDAO.get(email);

        usersDAO.setPassword((Integer) user.getId(), encryptPassword(password));

        JWTManager.disableToken(jwt);
    }
}
