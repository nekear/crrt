package com.github.DiachenkoMD.tests.service;

import com.github.DiachenkoMD.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.dto.*;
import com.github.DiachenkoMD.exceptions.DescriptiveException;
import com.github.DiachenkoMD.exceptions.ExceptionReason;
import com.github.DiachenkoMD.sevices.UsersService;
import com.github.DiachenkoMD.utils.Utils;
import com.github.DiachenkoMD.utils.flow_notifier.FlowNotifier;
import com.github.DiachenkoMD.utils.flow_notifier.Listener;
import com.google.gson.Gson;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.DiachenkoMD.utils.Utils.encrypt;
import static com.github.DiachenkoMD.utils.Utils.generateRandomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
public class UserService_test {
    @Mock
    private UsersDAO _usersDao;

    @Mock
    private FlowNotifier _fn;
    @Mock
    private Listener<DescriptiveException> _de_listener;
    @Captor
    private ArgumentCaptor<DescriptiveException> _de_;

    @InjectMocks
    private UsersService usersService;

    @Captor
    private ArgumentCaptor<String> _key_;
    @Captor
    private ArgumentCaptor<Status> _statusValue_;

    @Mock
    private HttpServletRequest _req;
    @Mock
    private HttpServletResponse _resp;
    @Mock
    private HttpSession _session;

    @BeforeEach
    public void beforeEach(){
        _fn.addListener(DescriptiveException.class, _de_listener);
    }

    @Nested
    @DisplayName("registerUser")
    class registerUserTests{

        private final String email = "test@gmail.com", password = "manitstest0204";
        private User processedUser;
        @BeforeEach
        public void setUp(){
            lenient().when(_req.getParameter("email")).thenReturn(email);
            lenient().when(_req.getParameter("password")).thenReturn(password);

            processedUser = new User(email, null, null, null);

            when(_req.getSession()).thenReturn(_session);
        }

        @Test
        @DisplayName("Registration successful")
        void testRegistrationSuccessful() throws IOException {
            when(_usersDao.doesExist(processedUser)).thenReturn(false);

            User _user = mock(User.class); // mock for bypassing if check
            when(_usersDao.register(processedUser, encrypt(password))).thenReturn(_user);
            when(_user.getId()).thenReturn("1");

            String confirmationCode = Utils.generateRandomString(12);
            when(_usersDao.generateConfirmationCode()).thenReturn(confirmationCode);
            when(_usersDao.setConfirmationCode(eq(email), eq(confirmationCode))).thenReturn(true);

            usersService.registerUser(_req, _resp);

            verify(_usersDao).doesExist(processedUser);
            verify(_usersDao).register(processedUser, encrypt(password));
            verify(_usersDao).generateConfirmationCode();
            verify(_usersDao).setConfirmationCode(eq(email), eq(confirmationCode));

            verify(_de_listener, never()).ping(any(DescriptiveException.class));
        }

        @Test
        @DisplayName("User already exists")
        void testUserAlreadyExists() throws IOException {
            when(_usersDao.doesExist(processedUser)).thenReturn(true);

            usersService.registerUser(_req, _resp);

            verify(_usersDao).doesExist(processedUser);
            verify(_usersDao, never()).register(processedUser, encrypt(password));

            verify(_de_listener).ping(_de_.capture());

            assertEquals(_de_.getValue().getReason(), ExceptionReason.EMAIL_EXISTS);

            verify(_resp).sendRedirect(anyString());
        }

        @ParameterizedTest
        @DisplayName("Failing validation")
        @MethodSource("provideInvalidParameters")
        void testUserDataFailValidation(String value, TValidationCategories category) throws IOException {
            if(category != TValidationCategories.email)
                lenient().when(_req.getParameter("email")).thenReturn("test@gmail.com");

            lenient().when(_req.getParameter(category.toString())).thenReturn(value);

            if(category != TValidationCategories.password)
                lenient().when(_req.getParameter("password")).thenReturn("nice_pass_123");

            usersService.registerUser(_req, _resp);

            verify(_usersDao, never()).doesExist(any(User.class));
            verify(_usersDao, never()).register(any(User.class), anyString());

            verify(_session).setAttribute(_key_.capture(), _statusValue_.capture());

            verify(_resp).sendRedirect(anyString());

            assertEquals(_statusValue_.getValue().getState(0), StatusStates.ERROR);
        }

        private static Stream<Arguments> provideInvalidParameters() {
            return Stream.of(
                    Arguments.of(
                            "some.gmail.com",
                            TValidationCategories.email
                    ),
                    Arguments.of(
                            "John12",
                            TValidationCategories.firstname
                    ),
                    Arguments.of(
                            "Doe2",
                            TValidationCategories.surname
                    ),
                    Arguments.of(
                            "Doevich0",
                            TValidationCategories.patronymic
                    ),
                    Arguments.of(
                            "badPassword",
                            TValidationCategories.password
                    )
            );
        }
        @Test
        @DisplayName("Registration process error")
        void testRegistrationProcessError(){
            when(_usersDao.doesExist(processedUser)).thenReturn(false);

            when(_usersDao.register(processedUser, encrypt(password))).thenReturn(null);

            usersService.registerUser(_req, _resp);

            verify(_session).setAttribute(_key_.capture(), _statusValue_.capture());

            assertEquals(_statusValue_.getValue().getClean(0).getText(), "global.unexpectedError");
            assertEquals(_statusValue_.getValue().getState(0), StatusStates.ERROR);
        }

        @Test
        @DisplayName("Confirmation code creation error")
        void testConfirmationCodeCreationError() {
            when(_usersDao.doesExist(processedUser)).thenReturn(false);

            User _user = mock(User.class); // mock for bypassing if check
            when(_usersDao.register(processedUser, encrypt(password))).thenReturn(_user);
            when(_user.getId()).thenReturn("1");

            String confirmationCode = Utils.generateRandomString(12);
            when(_usersDao.generateConfirmationCode()).thenReturn(confirmationCode);
            when(_usersDao.setConfirmationCode(eq(email), eq(confirmationCode))).thenReturn(false);
            usersService.registerUser(_req, _resp);

            verify(_session).setAttribute(_key_.capture(), _statusValue_.capture());

            assertEquals(_statusValue_.getValue().getClean(0).getText(), "global.unexpectedError");
            assertEquals(_statusValue_.getValue().getState(0), StatusStates.ERROR);
        }


        private enum TValidationCategories{email, firstname, surname, patronymic, password}
    }

    @Nested
    @DisplayName("confirmUserEmail")
    class confirmUserEmailTest{
        @Mock
        private ServletContext _servletContext;
        @Mock
        private RequestDispatcher _requestDispatcher;
        @BeforeEach
        public void onSetUp(){
            when(_req.getServletContext()).thenReturn(_servletContext);
            when(_servletContext.getRequestDispatcher(anyString())).thenReturn(_requestDispatcher);
        }
        @AfterEach
        public void onTearDown(){
            // Checking that function sends response to the correct place
            assertEquals(_key_.getValue(), "conf_res");
        }
        @Test
        @Order(1)
        @DisplayName("No code acquired fail")
        public void testNoCodeAcquiredFailTest(){
            when(_req.getParameter("code")).thenReturn(null);

            usersService.confirmUserEmail(_req, _resp);

            verify(_req).setAttribute(_key_.capture(), _statusValue_.capture());

            assertEquals(_statusValue_.getValue().getClean(0).getText(), "email_conf.code_empty");
            assertEquals(_statusValue_.getValue().getState(0), StatusStates.ERROR);
        }

        @Test
        @Order(2)
        @DisplayName("No user with code found")
        public void testNoUserWithCodeFound(){
            String code = "some_test_code";

            when(_req.getParameter("code")).thenReturn(code);

            when(_usersDao.getUserByConfirmationCode(eq(code))).thenReturn(null);

            usersService.confirmUserEmail(_req, _resp);

            verify(_req).setAttribute(_key_.capture(), _statusValue_.capture());

            assertEquals(_statusValue_.getValue().getClean(0).getText(), "email_conf.no_such_code");
            assertEquals(_statusValue_.getValue().getState(0), StatusStates.ERROR);
        }

        @Test
        @Order(3)
        @DisplayName("Setting code process fail")
        public void testSettingCodeProcessFail(){
            String code = "some_test_code";

            when(_req.getParameter("code")).thenReturn(code);

            User user = new User("test@gmail.com", null, null, null);

            when(_usersDao.getUserByConfirmationCode(eq(code))).thenReturn(user);

            when(_usersDao.setConfirmationCode(user.getEmail(), null)).thenReturn(false);

            usersService.confirmUserEmail(_req, _resp);

            verify(_req).setAttribute(_key_.capture(), _statusValue_.capture());

            assertEquals(_statusValue_.getValue().getClean(0).getText(), "email_conf.process_error");
            assertEquals(_statusValue_.getValue().getState(0), StatusStates.ERROR);
        }

        @Test
        @Order(4)
        @DisplayName("Confirmation success")
        public void testConfirmationSuccess(){
            String code = "some_test_code";

            when(_req.getParameter("code")).thenReturn(code);

            User user = new User("test@gmail.com", null, null, null);

            when(_usersDao.getUserByConfirmationCode(eq(code))).thenReturn(user);

            when(_usersDao.setConfirmationCode(user.getEmail(), null)).thenReturn(true);

            usersService.confirmUserEmail(_req, _resp);

            verify(_req).setAttribute(_key_.capture(), _statusValue_.capture());

            assertEquals(_statusValue_.getValue().getClean(0).getText(), "email_conf.conf_success");
            assertEquals(_statusValue_.getValue().getState(0), StatusStates.SUCCESS);
        }
    }

    @Nested
    @DisplayName("loginUser")
    class loginUserTest{
        @BeforeEach
        public void setUp() throws IOException{
            PrintWriter _printWriter = mock(PrintWriter.class);
            when(_resp.getWriter()).thenReturn(_printWriter);
        }

        @Test
        @DisplayName("Email validation fail")
        public void emailValidationFailTest() throws IOException{
            String email = "xp.com.test";
            String password = "random1234";
            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("email", email, "password", password)));

            usersService.loginUser(_req, _resp);

            verify(_resp).setStatus(500);
        }

        @Test
        @DisplayName("Password validation fail")
        public void passwordValidationFailTest() throws IOException{
            String email = "test@gmail.com";
            String password = "bp";
            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("email", email, "password", password)));

            usersService.loginUser(_req, _resp);

            verify(_resp).setStatus(500);
        }
        @Test
        @DisplayName("User not found fail")
        public void userNotFoundFailTest() throws IOException{
            String email = "test@gmail.com";
            String password = "pass1234";
            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("email", email, "password", password)));
            when(_usersDao.get(email)).thenReturn(null);

            usersService.loginUser(_req, _resp);

            verify(_resp).setStatus(500);
        }

        private static BufferedReader getBufferedReaderWithJson(Object obj){
            return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(new Gson().toJson(obj).getBytes())));
        }
    }

}
