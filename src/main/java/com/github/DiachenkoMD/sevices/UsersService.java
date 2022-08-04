package com.github.DiachenkoMD.sevices;

import com.github.DiachenkoMD.daos.DBTypes;
import com.github.DiachenkoMD.daos.factories.DAOFactory;
import com.github.DiachenkoMD.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.dto.*;
import com.github.DiachenkoMD.exceptions.DescriptiveException;

import static com.github.DiachenkoMD.utils.Utils.*;

import com.github.DiachenkoMD.exceptions.ExceptionReason;
import com.github.DiachenkoMD.utils.flow_notifier.FlowNotifier;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class UsersService {

    private static final Logger logger = LogManager.getLogger(UsersService.class);
    private final UsersDAO usersDAO;
    private final FlowNotifier fn;

    public UsersService(){
        DAOFactory factory = DAOFactory.getFactory(DBTypes.MYSQL);
        usersDAO = factory.getUsersDAO();
        this.fn = new FlowNotifier();

        fn.addListener(DescriptiveException.class, e -> logger.error(String.format("Caught an error because of %s", e.getReason()), e));
    }

    public UsersService(UsersDAO usersDAO, FlowNotifier fn){
        this.usersDAO = usersDAO;
        this.fn = fn;
    }

    /**
     * Service method for registering new users. In it`s <i>req</i> body should have json-data containing at least <i>email</i> and <i>password</i>. <br/>
     * Additionally, may contain <i>firstname</i>, <i>surname</i> and <i>patronymic</i>. <br/>
     * Incoming json-body should better contain unique (not already registered) email, otherwise this method will try to redirect to <i>/status</i> page and, additionally,
     * add cookie with <i>name="login_prg_message"</i> and <i>value="{some value from translation file}"</i>
     * @param req HttpServletRequest instance coming from controller
     * @param resp HttpServletResponse instance coming from controller
     */
    public void registerUser(HttpServletRequest req, HttpServletResponse resp) {
        
        logger.debug("Method entered from " + req.getRemoteAddr());
        
        try {
            // Gathering data from parameters
            String email = req.getParameter("email");
            String firstname = getIfNotEmpty(req.getParameter("firstname"));
            String surname = getIfNotEmpty(req.getParameter("surname"));
            String patronymic = getIfNotEmpty(req.getParameter("patronymic"));
            String password = getIfNotEmpty(req.getParameter("password"));

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
        }catch (DescriptiveException e){
            fn.omit_e(e);

            // DescriptiveException class has execute() method which accepts execution condition and action to be executed (which is Runnable by its nature)
            e.execute(ExceptionReason.EMAIL_EXISTS, () -> req.getSession().setAttribute("login_prg_message", new Status("sign_up.email_exists", true, e.getArguments(), StatusStates.ERROR)));
            e.execute(ExceptionReason.VALIDATION_ERROR, () -> req.getSession().setAttribute("login_prg_message", new Status("sign.validation_failed", true, StatusStates.ERROR)));
            e.execute(ExceptionReason.REGISTRATION_PROCESS_ERROR, () -> req.getSession().setAttribute("login_prg_message", new Status("global.unexpectedError", true, StatusStates.ERROR)));
            e.execute(ExceptionReason.CONFIRMATION_CODE_ERROR, () -> {
                logger.debug("Error setting code {} to {}", e.getArg("code"), e.getArg("email"));

                req.getSession().setAttribute("login_prg_message", new Status("global.unexpectedError", true, StatusStates.ERROR));
            });
        }catch (RuntimeException e){
            logger.error("Unexpected error", e);
            req.getSession().setAttribute("login_prg_message", new Status("global.unexpectedError", true, StatusStates.ERROR));
        }

        try {
            resp.sendRedirect("status");
        }catch (IOException ignored){}
    }

    public void confirmUserEmail(HttpServletRequest req, HttpServletResponse resp){
        String code = req.getParameter("code");

        try{
            if(code == null)
                throw new DescriptiveException(ExceptionReason.CONFIRMATION_CODE_EMPTY);

            User user = usersDAO.getUserByConfirmationCode(code);

            if(user == null)
                throw new DescriptiveException(ExceptionReason.CONFIRMATION_NO_SUCH_CODE);

            if(!usersDAO.setConfirmationCode(user.getEmail(), null))
                throw new DescriptiveException(ExceptionReason.CONFIRMATION_PROCESS_ERROR);

            req.setAttribute("conf_res", new Status("email_conf.conf_success", true, StatusStates.SUCCESS));
        }catch (DescriptiveException e){
            e.execute(ExceptionReason.CONFIRMATION_CODE_EMPTY, () -> req.setAttribute("conf_res", new Status("email_conf.code_empty", true, StatusStates.ERROR)));
            e.execute(ExceptionReason.CONFIRMATION_NO_SUCH_CODE, () -> req.setAttribute("conf_res", new Status("email_conf.no_such_code", true, StatusStates.ERROR)));
            e.execute(ExceptionReason.CONFIRMATION_PROCESS_ERROR, () -> req.setAttribute("conf_res", new Status("email_conf.process_error", true, StatusStates.ERROR)));
        }

        try{
            req.getServletContext().getRequestDispatcher("/views/confirmation.jsp").forward(req, resp);
        }catch (IOException | ServletException e){
            logger.error(e);
        }
    }

    public void loginUser(HttpServletRequest req, HttpServletResponse resp){
        try{
            String requestData = req.getReader().lines().collect(Collectors.joining());

            JSONObject acquiredData = new JSONObject(requestData);

            String email = acquiredData.getString("email");
            String password = acquiredData.getString("password");

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

            req.getSession().setAttribute("auth", user);

            resp.setStatus(200);
            resp.getWriter().flush();
        }catch (DescriptiveException | IOException e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            if(e instanceof DescriptiveException){
                logger.error(e);

                DescriptiveException desc_e = (DescriptiveException) e;

                desc_e.execute(ExceptionReason.VALIDATION_ERROR, () -> exceptionToClient.set(new StatusText("login.validation_failed", true, StatusStates.ERROR).convert("en")));
                desc_e.execute(ExceptionReason.LOGIN_USER_NOT_FOUND, () -> exceptionToClient.set(new StatusText("login.incorrect_data", true, StatusStates.ERROR).convert("en")));
                desc_e.execute(ExceptionReason.LOGIN_NOT_CONFIRMED, () -> exceptionToClient.set(new StatusText("login.account_not_confirmed", true, StatusStates.ERROR).convert("en")));
                desc_e.execute(ExceptionReason.LOGIN_WRONG_PASSWORD, () -> exceptionToClient.set(new StatusText("login.incorrect_data", true, StatusStates.ERROR).convert("en")));

            }else {
                exceptionToClient.set(new StatusText("global.unexpectedError", true, StatusStates.ERROR).convert("en"));
                logger.error(e);
            }

            try{
                resp.setStatus(500);
                resp.getWriter().write(exceptionToClient.get());
                resp.getWriter().flush();
            }catch (IOException io_e){
                logger.error(io_e);
            }
        }
    }
}
