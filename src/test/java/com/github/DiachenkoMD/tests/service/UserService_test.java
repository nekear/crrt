package com.github.DiachenkoMD.tests.service;

import com.github.DiachenkoMD.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.dto.*;
import com.github.DiachenkoMD.sevices.UsersService;
import com.github.DiachenkoMD.tests.utils.GlobalUtilsTest;
import com.github.DiachenkoMD.utils.TServletInputStream;
import com.github.DiachenkoMD.utils.Utils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Stream;

import static com.github.DiachenkoMD.utils.Utils.encrypt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
public class UserService_test {
    @Mock
    private UsersDAO _usersDao;

    @InjectMocks
    private UsersService usersService;

    @Captor
    private ArgumentCaptor<String> _sessionKey;
    @Captor
    private ArgumentCaptor<Status> _sessionStatusValue;

    @Mock
    private HttpServletRequest _req;
    @Mock
    private HttpServletResponse _resp;
    @Mock
    private HttpSession _session;
    @BeforeEach
    public void beforeEach() throws IOException{
        when(_req.getSession()).thenReturn(_session);
        doNothing().when(_resp).sendRedirect(anyString()); // blocking redirect just not to render jsp
    }

    @Nested
    @DisplayName("registerUser")
    class registerUserTests{
        @Test
        @DisplayName("Registration successful")
        void testRegistrationSuccessful() throws IOException {
            String email = "test@gmail.com";
            String password = "manitstest0204";

            lenient().when(_req.getParameter("email")).thenReturn(email);
            lenient().when(_req.getParameter("password")).thenReturn(password);

            User processedUser = new User(email, null, null, null);

            when(_usersDao.doesExist(processedUser)).thenReturn(false);

            usersService.registerUser(_req, _resp);

            verify(_usersDao).doesExist(processedUser);
            verify(_usersDao).register(processedUser, encrypt(password));

            verify(_session).setAttribute(_sessionKey.capture(), _sessionStatusValue.capture());

            assertEquals(_sessionStatusValue.getValue().getState(0), StatusStates.SUCCESS);
        }

        @Test
        @DisplayName("User already exists")
        void testUserAlreadyExists() throws IOException {
            String email = "test@gmail.com";
            String password = "manitstest0204";

            lenient().when(_req.getParameter("email")).thenReturn(email);
            lenient().when(_req.getParameter("password")).thenReturn(password);

            User processedUser = new User(email, null, null, null);

            when(_usersDao.doesExist(processedUser)).thenReturn(true);

            usersService.registerUser(_req, _resp);

            verify(_usersDao).doesExist(processedUser);
            verify(_usersDao, never()).register(processedUser, encrypt(password));

            verify(_session).setAttribute(_sessionKey.capture(), _sessionStatusValue.capture());

            verify(_resp).sendRedirect(anyString());

            assertEquals(_sessionStatusValue.getValue().getState(0), StatusStates.ERROR);
        }

        @ParameterizedTest
        @DisplayName("Password fail validation")
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

            verify(_session).setAttribute(_sessionKey.capture(), _sessionStatusValue.capture());

            verify(_resp).sendRedirect(anyString());

            assertEquals(_sessionStatusValue.getValue().getState(0), StatusStates.ERROR);
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

        private enum TValidationCategories{email, firstname, surname, patronymic, password}
    }
}
