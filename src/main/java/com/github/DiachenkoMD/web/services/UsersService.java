package com.github.DiachenkoMD.web.services;

import static com.github.DiachenkoMD.entities.Constants.*;
import com.github.DiachenkoMD.entities.dto.*;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.web.services.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;

import static com.github.DiachenkoMD.web.utils.Utils.*;

import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
    public void registerUser(HttpServletRequest req, HttpServletResponse resp) throws DescriptiveException, DBException{
        
        logger.debug("Method entered from {}", req.getRemoteAddr());

        // Gathering data from parameters
        String email = cleanGetParameter(req.getParameter(REQ_EMAIL));
        String firstname = cleanGetParameter(req.getParameter(REQ_FIRSTNAME));
        String surname = cleanGetParameter(req.getParameter(REQ_SURNAME));
        String patronymic = cleanGetParameter(req.getParameter(REQ_PATRONYMIC));
        String password = cleanGetParameter(req.getParameter(REQ_PASSWORD));

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
        User registeringUser = new User(email, firstname, surname, patronymic);

        // Checking user for existence
        boolean doesExist = usersDAO.doesExist(registeringUser);
        if (doesExist)
            throw new DescriptiveException(new HashMap<>(Map.of("email", email)), ExceptionReason.EMAIL_EXISTS);

        // Registering new user (method returns original user entity + newly created id included)
        User registeredUserEntity = usersDAO.register(registeringUser, encrypt(password));

        if(registeredUserEntity == null || registeredUserEntity.getId() == null)
            throw new DescriptiveException("Error while registering user", ExceptionReason.REGISTRATION_PROCESS_ERROR);

        String confirmationCode = usersDAO.generateConfirmationCode();

        if(!usersDAO.setConfirmationCode(registeringUser.getEmail(), confirmationCode))
            throw new DescriptiveException(new HashMap<>(Map.of("email", email, "code", confirmationCode)), ExceptionReason.CONFIRMATION_CODE_ERROR);

        emailNotify(registeringUser, "Account confirmation email from CRRT", "You can confirm your code at http://localhost:8080/crrt_war/confirmation?code="+confirmationCode);

        req.getSession().setAttribute("login_prg_message", new Status("sign_up.verify_email", true, new HashMap<>(Map.of("email", email)), StatusStates.SUCCESS));

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

        User user = usersDAO.getUserByConfirmationCode(code);

        if(user == null)
            throw new DescriptiveException(ExceptionReason.CONFIRMATION_NO_SUCH_CODE);

        if(!usersDAO.setConfirmationCode(user.getEmail(), null))
            throw new DescriptiveException(ExceptionReason.CONFIRMATION_PROCESS_ERROR);

        req.setAttribute(END_CONFIRMATION_RESPONSE, new Status("email_conf.conf_success", true, StatusStates.SUCCESS));
    }

    /**
     * Service method for logging users into account. <br/>
     * Must contain {@link com.github.DiachenkoMD.entities.Constants#REQ_EMAIL REQ_EMAIL} and {@link com.github.DiachenkoMD.entities.Constants#REQ_PASSWORD REQ_PASSWORD} parameters, otherwise will send error response to user.
     * @param req HttpServletRequest instance coming from controller
     * @param resp HttpServletResponse instance coming from controller
     */
    public void loginUser(HttpServletRequest req, HttpServletResponse resp) throws DescriptiveException, IOException, DBException {

        String requestData = req.getReader().lines().collect(Collectors.joining());

        JSONObject acquiredData = new JSONObject(requestData);

        String email = acquiredData.getString(REQ_EMAIL);
        String password = acquiredData.getString(REQ_PASSWORD);

        logger.debug("Email: {} and password: {}", email, password);

        if(!validate(email, ValidationParameters.EMAIL))
            throw new DescriptiveException("Email validation failed", ExceptionReason.VALIDATION_ERROR);

        if(!validate(password, ValidationParameters.PASSWORD))
            throw new DescriptiveException("Password validation failed", ExceptionReason.VALIDATION_ERROR);

        User user = usersDAO.get(email);

        if(user == null)
            throw new DescriptiveException("User with such email was not found", ExceptionReason.LOGIN_USER_NOT_FOUND);

        if(user.getConfirmationCode() != null)
            throw new DescriptiveException("This account was not confirmed by email", ExceptionReason.LOGIN_NOT_CONFIRMED);

        if(!encryptedCompare(password, user.getPassword()))
            throw new DescriptiveException("Password or email are invalid", ExceptionReason.LOGIN_WRONG_PASSWORD);

        user.setPassword(null); // removing password from session entity

        req.getSession().setAttribute(SESSION_AUTH, user);

        resp.setStatus(200);
    }
}
