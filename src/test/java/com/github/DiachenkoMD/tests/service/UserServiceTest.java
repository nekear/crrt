package com.github.DiachenkoMD.tests.service;

import static com.github.DiachenkoMD.entities.Constants.*;

import com.github.DiachenkoMD.entities.dto.StatusStates;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.web.services.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.entities.dto.Status;
import com.github.DiachenkoMD.entities.dto.User;
import com.github.DiachenkoMD.web.services.UsersService;
import com.github.DiachenkoMD.web.utils.Utils;
import com.google.gson.Gson;
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

import static com.github.DiachenkoMD.web.utils.Utils.encrypt;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
public class UserServiceTest {
    @Mock
    private UsersDAO _usersDao;

    @InjectMocks
    private UsersService usersService;

    @Captor
    private ArgumentCaptor<String> _stringCaptor;
    @Captor
    private ArgumentCaptor<Status> _statusCaptor;

    @Mock
    private HttpServletRequest _req;
    @Mock
    private HttpServletResponse _resp;
    @Mock
    private HttpSession _session;


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
        }

        @Test
        @DisplayName("Registration successful")
        void testRegistrationSuccessful() throws Exception {
            when(_usersDao.doesExist(processedUser)).thenReturn(false);

            User _user = mock(User.class); // mock for bypassing if check
            when(_usersDao.register(processedUser, encrypt(password))).thenReturn(_user);
            when(_user.getId()).thenReturn("1");

            String confirmationCode = Utils.generateRandomString(12);

            when(_usersDao.generateConfirmationCode()).thenReturn(confirmationCode);

            when(_usersDao.setConfirmationCode(email, confirmationCode)).thenReturn(true);

            when(_req.getSession()).thenReturn(_session);

            assertDoesNotThrow(() -> usersService.registerUser(_req, _resp));

            // For successful registration, we should 1) check for user existence, 2) register him 3) generate confirmation code and 4) set that code into db
            verify(_usersDao).doesExist(processedUser);
            verify(_usersDao).register(processedUser, encrypt(password));
            verify(_usersDao).generateConfirmationCode();
            verify(_usersDao).setConfirmationCode(email, confirmationCode);
        }

        @Test
        @DisplayName("User already exists")
        void testUserAlreadyExists() throws Exception {
            when(_usersDao.doesExist(processedUser)).thenReturn(true);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.registerUser(_req, _resp));

            assertEquals(ExceptionReason.EMAIL_EXISTS, expectedException.getReason());

            verify(_usersDao).doesExist(processedUser);
            verify(_usersDao, never()).register(processedUser, encrypt(password));
        }

        @ParameterizedTest
        @DisplayName("Failing validation")
        @MethodSource("provideInvalidParameters")
        void testUserDataFailValidation(String value, TValidationCategories category) throws Exception {
            if(category != TValidationCategories.email)
                lenient().when(_req.getParameter("email")).thenReturn("test@gmail.com");

            lenient().when(_req.getParameter(category.toString())).thenReturn(value);

            if(category != TValidationCategories.password)
                lenient().when(_req.getParameter("password")).thenReturn("nice_pass_123");

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.registerUser(_req, _resp));

            assertEquals(ExceptionReason.VALIDATION_ERROR, expectedException.getReason());

            verify(_usersDao, never()).doesExist(any(User.class));
            verify(_usersDao, never()).register(any(User.class), anyString());
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
        void testRegistrationProcessError() throws Exception{
            when(_usersDao.doesExist(processedUser)).thenReturn(false);

            when(_usersDao.register(processedUser, encrypt(password))).thenReturn(null);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.registerUser(_req, _resp));

            assertEquals(ExceptionReason.REGISTRATION_PROCESS_ERROR, expectedException.getReason());
        }

        @Test
        @DisplayName("Confirmation code creation error")
        void testConfirmationCodeCreationError()  throws Exception{
            when(_usersDao.doesExist(processedUser)).thenReturn(false);

            User _user = mock(User.class); // mock for bypassing if check
            when(_usersDao.register(processedUser, encrypt(password))).thenReturn(_user);
            when(_user.getId()).thenReturn("1");

            String confirmationCode = Utils.generateRandomString(12);
            when(_usersDao.generateConfirmationCode()).thenReturn(confirmationCode);
            when(_usersDao.setConfirmationCode(email, confirmationCode)).thenReturn(false);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.registerUser(_req, _resp));

            assertEquals(ExceptionReason.CONFIRMATION_CODE_ERROR, expectedException.getReason());
        }

        private enum TValidationCategories{email, firstname, surname, patronymic, password}
    }

    @Nested
    @DisplayName("confirmUserEmail")
    class confirmUserEmailTest{
        @Test
        @Order(1)
        @DisplayName("No code acquired fail")
        void testNoCodeAcquiredFailTest() throws Exception{
            when(_req.getParameter("code")).thenReturn(null);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.confirmUserEmail(_req, _resp));

            assertEquals(ExceptionReason.CONFIRMATION_CODE_EMPTY, expectedException.getReason());
        }

        @Test
        @Order(2)
        @DisplayName("No user with code found")
        void testNoUserWithCodeFound() throws Exception{
            String code = "some_test_code";

            when(_req.getParameter("code")).thenReturn(code);

            when(_usersDao.getUserByConfirmationCode(code)).thenReturn(null);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.confirmUserEmail(_req, _resp));

            assertEquals(ExceptionReason.CONFIRMATION_NO_SUCH_CODE, expectedException.getReason());
        }

        @Test
        @Order(3)
        @DisplayName("Setting code process fail")
        void testSettingCodeProcessFail() throws Exception{
            String code = "some_test_code";

            when(_req.getParameter("code")).thenReturn(code);

            User user = new User("test@gmail.com", null, null, null);

            when(_usersDao.getUserByConfirmationCode(code)).thenReturn(user);

            when(_usersDao.setConfirmationCode(user.getEmail(), null)).thenReturn(false);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.confirmUserEmail(_req, _resp));

            assertEquals(ExceptionReason.CONFIRMATION_PROCESS_ERROR, expectedException.getReason());
        }

        @Test
        @Order(4)
        @DisplayName("Confirmation success")
        void testConfirmationSuccess() throws Exception{
            String code = "some_test_code";

            when(_req.getParameter(REQ_CODE)).thenReturn(code);

            User user = new User("test@gmail.com", null, null, null);

            when(_usersDao.getUserByConfirmationCode(code)).thenReturn(user);

            when(_usersDao.setConfirmationCode(user.getEmail(), null)).thenReturn(true);

            assertDoesNotThrow(()  -> usersService.confirmUserEmail(_req, _resp));

            verify(_req).setAttribute(_stringCaptor.capture(), _statusCaptor.capture());

            assertEquals(END_CONFIRMATION_RESPONSE, _stringCaptor.getValue());
            assertEquals(StatusStates.SUCCESS, _statusCaptor.getValue().getState(0));
        }
    }

    @Nested
    @DisplayName("loginUser")
    class loginUserTest{
        @Test
        @DisplayName("Email validation fail")
        void emailValidationFailTest() throws Exception{
            String email = "xp.com.test";
            String password = "random1234";

            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("email", email, "password", password)));

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.loginUser(_req, _resp));

            assertEquals(ExceptionReason.VALIDATION_ERROR, expectedException.getReason());
        }

        @Test
        @DisplayName("Password validation fail")
        void passwordValidationFailTest() throws Exception{
            String email = "test@gmail.com";
            String password = "bp";
            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("email", email, "password", password)));

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.loginUser(_req, _resp));

            assertEquals(ExceptionReason.VALIDATION_ERROR, expectedException.getReason());
        }
        @Test
        @DisplayName("User not found fail")
        void userNotFoundFailTest() throws Exception{
            String email = "test@gmail.com";
            String password = "pass1234";

            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("email", email, "password", password)));
            when(_usersDao.get(email)).thenReturn(null);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.loginUser(_req, _resp));

            assertEquals(ExceptionReason.LOGIN_USER_NOT_FOUND, expectedException.getReason());
        }

        @Test
        @DisplayName("Login successful")
        void loginSuccessfulTest() throws Exception{
            String email = "test@gmail.com";
            String password = "pass1234";

            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("email", email, "password", password)));

            User _user = mock(User.class);

            when(_usersDao.get(email)).thenReturn(_user); // to bypass checks

            when(_user.getPassword()).thenReturn(Utils.encrypt(password)); // to bypass checks

            when(_req.getSession()).thenReturn(_session); // for final session actions

            assertDoesNotThrow(() -> usersService.loginUser(_req, _resp));

            // Checking for setting password to null (we don`t want expose it to client somehow)
            verify(_user).setPassword(null);

            // Checking for setting user into session variable called {Constants.SESSION_AUTH}
            verify(_req.getSession()).setAttribute(SESSION_AUTH, _user);
        }
        private static BufferedReader getBufferedReaderWithJson(Object obj){
            return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(new Gson().toJson(obj).getBytes())));
        }
    }

}
