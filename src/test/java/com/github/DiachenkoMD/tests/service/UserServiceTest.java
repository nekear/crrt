package com.github.DiachenkoMD.tests.service;

import static com.github.DiachenkoMD.entities.Constants.*;

import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.entities.dto.Status;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.web.services.UsersService;
import com.github.DiachenkoMD.web.utils.RecaptchaVerifier;
import com.github.DiachenkoMD.web.utils.RightsManager;
import com.github.DiachenkoMD.web.utils.Utils;
import com.google.gson.Gson;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

import static com.github.DiachenkoMD.web.utils.Utils.encryptPassword;
import static com.github.DiachenkoMD.web.utils.Utils.generateRandomString;
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
        private AuthUser processedUser;

        @BeforeAll
        public static void beforeAllRegs(){
            RecaptchaVerifier.setTestingMode();
        }

        @AfterAll
        public static void afterAllRegs(){
            RecaptchaVerifier.setRealMode();
        }

        @BeforeEach
        public void setUp(){
            lenient().when(_req.getParameter("email")).thenReturn(email);
            lenient().when(_req.getParameter("password")).thenReturn(password);
            lenient().when(_req.getParameter("g-recaptcha-response")).thenReturn("someRandomRecaptchaFake");

            processedUser = AuthUser.of(email, null, null, null);
        }

        @Test
        @DisplayName("Registration successful")
        void testRegistrationSuccessful() throws Exception {
            when(_usersDao.doesExist(processedUser)).thenReturn(false);

            AuthUser _user = mock(AuthUser.class); // mock for bypassing if check
            when(_usersDao.completeRegister(processedUser, encryptPassword(password))).thenReturn(_user);
            when(_user.getId()).thenReturn(1);

            assertDoesNotThrow(() -> usersService.registerUser(_req, _resp));

            // For successful registration, we should 1) check for user existence, 2) register him (confirmation code is added inside completeRegister method)
            verify(_usersDao).doesExist(processedUser);
            verify(_usersDao).completeRegister(processedUser, encryptPassword(password));
        }

        @Test
        @DisplayName("User already exists")
        void testUserAlreadyExists() throws Exception {
            when(_usersDao.doesExist(processedUser)).thenReturn(true);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.registerUser(_req, _resp));

            assertEquals(ExceptionReason.EMAIL_EXISTS, expectedException.getReason());

            verify(_usersDao).doesExist(processedUser);
            verify(_usersDao, never()).completeRegister(processedUser, encryptPassword(password));
        }

        @Test
        @DisplayName("Recaptcha verification fail")
        void recaptchaVerificationFail() throws Exception {
            when(_usersDao.doesExist(processedUser)).thenReturn(true);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.registerUser(_req, _resp));

            assertEquals(ExceptionReason.EMAIL_EXISTS, expectedException.getReason());

            verify(_usersDao).doesExist(processedUser);
            verify(_usersDao, never()).completeRegister(processedUser, encryptPassword(password));
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

            verify(_usersDao, never()).doesExist(any(AuthUser.class));
            verify(_usersDao, never()).completeRegister(any(AuthUser.class), anyString());
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

            when(_usersDao.completeRegister(processedUser, encryptPassword(password))).thenReturn(null);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.registerUser(_req, _resp));

            assertEquals(ExceptionReason.REGISTRATION_PROCESS_ERROR, expectedException.getReason());
        }
    }

    @Nested
    @DisplayName("confirmUserEmail")
    class confirmUserEmailTests{
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

            AuthUser user = AuthUser.of("test@gmail.com", null, null, null);
            user.setId(1);

            when(_usersDao.getUserByConfirmationCode(code)).thenReturn(user);

            when(_usersDao.setConfirmationCode(user.getCleanId().get(), null)).thenReturn(false);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.confirmUserEmail(_req, _resp));

            assertEquals(ExceptionReason.CONFIRMATION_PROCESS_ERROR, expectedException.getReason());
        }

        @Test
        @Order(4)
        @DisplayName("Confirmation success")
        void testConfirmationSuccess() throws Exception{
            String code = "some_test_code";

            when(_req.getParameter(REQ_CODE)).thenReturn(code);

            AuthUser user = AuthUser.of("test@gmail.com", null, null, null);
            user.setId(1);

            when(_usersDao.getUserByConfirmationCode(code)).thenReturn(user);

            when(_usersDao.setConfirmationCode(user.getCleanId().get(), null)).thenReturn(true);

            assertDoesNotThrow(()  -> usersService.confirmUserEmail(_req, _resp));
        }
    }

    @Nested
    @DisplayName("loginUser")
    class loginUserTests{
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
            ServletContext tmpCtxMock = mock(ServletContext.class);
            when(_req.getServletContext()).thenReturn(tmpCtxMock);
            when(tmpCtxMock.getAttribute("rights_manager")).thenReturn(mock(RightsManager.class));

            AuthUser _user = mock(AuthUser.class);

            when(_user.getCleanId()).thenReturn(Optional.of(1));
            when(_user.getId()).thenReturn(1);
            when(_usersDao.get(email)).thenReturn(_user); // to bypass checks

            when(_usersDao.getPassword(anyInt())).thenReturn(Utils.encryptPassword(password)); // to bypass checks

            assertDoesNotThrow(() -> usersService.loginUser(_req, _resp));
        }

    }

    @Nested
    @DisplayName("updateData")
    class updateDataTests{
        private AuthUser user;
        @BeforeEach
        public void setUp(){
            lenient().when(_req.getSession()).thenReturn(_session);

            user = AuthUser.of("test@gmail.com", null, null, null);
            user.setId(1);

            lenient().when(_session.getAttribute(SESSION_AUTH)).thenReturn(user);
        }
        @Test
        @DisplayName("Successful update")
        void successfulUserDataUpdateTest() throws Exception{
            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("firstname", "Mykhailo", "surname", "Diachenko")));

            user.setFirstname("Mykhailo");
            user.setSurname("Diachenko");

            when(_usersDao.get(user.getEmail())).thenReturn(user);

            assertEquals(user, usersService.updateData(_req, _resp));
        }

        @Test
        @DisplayName("Empty incoming data fail")
        void emptyIncomingDataFailTest() throws Exception{
            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of()));

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.updateData(_req, _resp));

            assertEquals(ExceptionReason.VALIDATION_ERROR, expectedException.getReason());
        }

        @ParameterizedTest
        @DisplayName("Input data validation fail")
        @MethodSource("provideInvalidParameters")
        void emptyIncomingDataFailTest(String value, TValidationCategories category) throws Exception{
            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of(category, value)));

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.updateData(_req, _resp));

            assertEquals(ExceptionReason.VALIDATION_ERROR, expectedException.getReason());
        }

        private static Stream<Arguments> provideInvalidParameters() {
            return Stream.of(
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
                    )
            );
        }
    }

    @Nested
    @DisplayName("updatePassword")
    class updatePasswordTests{
        private AuthUser user;
        @BeforeEach
        public void setUp(){
            lenient().when(_req.getSession()).thenReturn(_session);

            user = AuthUser.of("test@gmail.com", null, null, null);
            user.setId(1);

            lenient().when(_session.getAttribute(SESSION_AUTH)).thenReturn(user);
        }
        @Test
        @DisplayName("Successful update")
        void successfulPasswordUpdateTest() throws Exception{
            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("old_password", "pass1old", "new_password", "pass2new")));

            when(_usersDao.getPassword(user.getCleanId().get())).thenReturn(encryptPassword("pass1old"));

            when(_usersDao.setPassword(eq(user.getCleanId().get()), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> usersService.updatePassword(_req, _resp));
        }

        @Test
        @DisplayName("Passwords don`t match fail")
        void passwordsDontMatchTest() throws Exception{
            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("old_password", "pass1old", "new_password", "pass2new")));

            when(_usersDao.getPassword(user.getCleanId().get())).thenReturn(encryptPassword("pass3real"));

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.updatePassword(_req, _resp));

            assertEquals(ExceptionReason.UUD_PASSWORDS_DONT_MATCH, expectedException.getReason());
        }

        @Test
        @DisplayName("New password validation fail")
        void newPasswordValidationFailTest() throws Exception{
            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("old_password", "pass1old", "new_password", "2")));

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.updatePassword(_req, _resp));

            assertEquals(ExceptionReason.VALIDATION_ERROR, expectedException.getReason());
        }
        @Test
        @DisplayName("Updating passwords process fail")
        void updatingPasswordsProcessFailTest() throws Exception{
            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("old_password", "pass1old", "new_password", "pass2new")));

            when(_usersDao.getPassword(user.getCleanId().get())).thenReturn(encryptPassword("pass1old"));

            when(_usersDao.setPassword(eq(user.getCleanId().get()), anyString())).thenReturn(false);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.updatePassword(_req, _resp));

            assertEquals(ExceptionReason.DB_ACTION_ERROR, expectedException.getReason());
        }
    }

    @Nested
    @DisplayName("replenishBalance")
    class replenishBalanceTests{
        @Spy
        private AuthUser _user_ = new AuthUser();
        @BeforeEach
        public void setUp(){
            lenient().when(_req.getSession()).thenReturn(_session);
            lenient().when(_session.getAttribute(SESSION_AUTH)).thenReturn(_user_);
            _user_.setId(1);
        }
        @Test
        @DisplayName("Successful replenishment")
        void successfulReplenishmentTest() throws Exception{
            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("amount", 10)));

            when(_usersDao.getBalance((Integer) _user_.getId())).thenReturn(5d);

            when(_usersDao.setBalance((Integer) _user_.getId(), 15d)).thenReturn(true);

            assertDoesNotThrow(() -> usersService.replenishBalance(_req, _resp));

            verify(_user_).setBalance(15d);
        }

        @Test
        @DisplayName("Amount validation fail")
        void amountValidationFailTest() throws Exception{
            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("amount", -20)));

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.replenishBalance(_req, _resp));

            assertEquals(ExceptionReason.VALIDATION_ERROR, expectedException.getReason());

            verify(_user_, never()).setBalance(anyInt());
        }

        @Test
        @DisplayName("Updating amount process fail")
        void updatingAmountProcessFail() throws Exception{
            when(_req.getReader()).thenReturn(getBufferedReaderWithJson(Map.of("amount", 10)));

            when(_usersDao.getBalance((Integer) _user_.getId())).thenReturn(5d);

            when(_usersDao.setBalance((Integer) _user_.getId(), 15d)).thenReturn(false);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.replenishBalance(_req, _resp));

            assertEquals(ExceptionReason.DB_ACTION_ERROR, expectedException.getReason());

            verify(_user_, never()).setBalance(anyInt());
        }
    }

    @Nested
    @DisplayName("uploadAvatar")
    class uploadAvatarTests{

        @Mock
        private Part _filePart;

        private Collection<Part> _parts_;
        @Spy
        private AuthUser _user_ = new AuthUser();

        @Mock
        private ServletContext _servletContext;
        @BeforeEach
        public void setUp() throws ServletException, IOException {

            // Configuring session to bypass getting of the user
            lenient().when(_req.getSession()).thenReturn(_session);
            lenient().when(_session.getAttribute(SESSION_AUTH)).thenReturn(_user_);
            _user_.setId(1);

            // Configuring servlet context to get file path
            lenient().when(_req.getServletContext()).thenReturn(_servletContext);
            lenient().when(_servletContext.getRealPath(AVATAR_UPLOAD_DIR)).thenReturn(new File(".").getAbsolutePath()+AVATAR_UPLOAD_DIR);

            // Configuring file acquirement
            _parts_ = spy(new ArrayList<>(List.of(_filePart)));
            when(_req.getParts()).thenReturn(_parts_); // to bypass size() check
            when(_req.getPart(anyString())).thenReturn(_filePart);
        }
        @Test
        @DisplayName("Successful uploading")
        void successfulUploadingTest() throws Exception{
            when(_filePart.getSize()).thenReturn((long) (1024 * 1024));
            when(_filePart.getSubmittedFileName()).thenReturn("file.jpg");

            when(_usersDao.getAvatar((Integer) _user_.getId())).thenReturn(Optional.empty());

            when(_usersDao.setAvatar(eq((Integer) _user_.getId()), anyString())).thenReturn(true);

            assertInstanceOf(String.class, usersService.uploadAvatar(_req, _resp));

            verify(_user_).setAvatar(anyString());
            verify(_filePart).write(anyString());
        }

        @Test
        @DisplayName("Too many files fail")
        void tooManyFilesFailTest() throws Exception{
            when(_parts_.size()).thenReturn(3); // <----

            lenient().when(_filePart.getSize()).thenReturn((long) (1024 * 1024));
            lenient().when(_filePart.getSubmittedFileName()).thenReturn("file.jpg");

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.uploadAvatar(_req, _resp));

            verify(_user_, never()).setAvatar(anyString());
            verify(_filePart, never()).write(anyString());

            assertEquals(ExceptionReason.TOO_MANY_FILES, expectedException.getReason());
        }

        @Test
        @DisplayName("File size is larger than allowed fail")
        void fileSizeLargerAllowedFailTest() throws Exception{
            when(_filePart.getSize()).thenReturn((long) (1024 * 1024 * 20)); // <----
            lenient().when(_filePart.getSubmittedFileName()).thenReturn("file.jpg");

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.uploadAvatar(_req, _resp));

            verify(_user_, never()).setAvatar(anyString());
            verify(_filePart, never()).write(anyString());

            assertEquals(ExceptionReason.TOO_BIG_FILE_SIZE, expectedException.getReason());
        }

        @Test
        @DisplayName("File should end with .jpg or .png fail")
        void fileEndingFailTest() throws Exception{
            lenient().when(_filePart.getSize()).thenReturn((long) (1024 * 1024));
            when(_filePart.getSubmittedFileName()).thenReturn("file.pdf"); // <----

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.uploadAvatar(_req, _resp));

            verify(_user_, never()).setAvatar(anyString());
            verify(_filePart, never()).write(anyString());

            assertEquals(ExceptionReason.BAD_FILE_EXTENSION, expectedException.getReason());
        }

        @Test
        @DisplayName("Setting avatar name to db fail")
        void settingAvatarNameToDBFailTest() throws Exception{
            when(_filePart.getSize()).thenReturn((long) (1024 * 1024));
            when(_filePart.getSubmittedFileName()).thenReturn("file.jpg");

            when(_usersDao.getAvatar((Integer) _user_.getId())).thenReturn(Optional.empty());

            when(_usersDao.setAvatar(eq((Integer) _user_.getId()), anyString())).thenReturn(false);

            DescriptiveException expectedException = assertThrows(DescriptiveException.class, () -> usersService.uploadAvatar(_req, _resp));

            verify(_user_, never()).setAvatar(anyString());

            assertEquals(ExceptionReason.DB_ACTION_ERROR, expectedException.getReason());
        }
    }

    @Nested
    @DisplayName("deleteAvatar")
    class deleteAvatarTests{

        @Spy
        private AuthUser _user_ = new AuthUser();

        @Mock
        private ServletContext _servletContext;
        @BeforeEach
        public void setUp() throws ServletException, IOException {

            // Configuring session to bypass getting of the user
            lenient().when(_req.getSession()).thenReturn(_session);
            lenient().when(_session.getAttribute(SESSION_AUTH)).thenReturn(_user_);
            _user_.setId(1);

            // Configuring servlet context to get file path
            lenient().when(_req.getServletContext()).thenReturn(_servletContext);
            lenient().when(_servletContext.getRealPath(AVATAR_UPLOAD_DIR)).thenReturn(new File(".").getAbsolutePath()+AVATAR_UPLOAD_DIR);
        }
        @Test
        @DisplayName("Return null avatar link if nothing to delete")
        void successfulUploadingTest() throws Exception{
            when(_usersDao.getAvatar((Integer) _user_.getId())).thenReturn(Optional.empty());

            assertEquals(_user_.idenAvatar("doesntmatter"), usersService.deleteAvatar(_req, _resp));

            verify(_usersDao, never()).setAvatar(anyInt(), eq(null));
        }
    }

    private static BufferedReader getBufferedReaderWithJson(Object obj){
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(new Gson().toJson(obj).getBytes())));
    }

    private enum TValidationCategories{email, firstname, surname, patronymic, password}
}
