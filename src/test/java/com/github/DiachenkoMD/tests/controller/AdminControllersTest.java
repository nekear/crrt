package com.github.DiachenkoMD.tests.controller;

import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.utils.TStore;
import com.github.DiachenkoMD.web.controllers.admin.StatsController;
import com.github.DiachenkoMD.web.controllers.admin.cars_related.CarController;
import com.github.DiachenkoMD.web.services.AdminService;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unlike DAO tests and Services test, here little of tests were created just to show one way of testing
 * controllers. <br/>
 * I do not see a great need in the controllers test,
 * their work is the executing of simple actions in which bugs almost can not get.
 */
@ExtendWith({
        MockitoExtension.class
})
public class AdminControllersTest {
    @Mock
    private HttpServletRequest _req;

    @Mock
    private HttpServletResponse _resp;
    @Mock
    private PrintWriter _writer;
    private static AdminService _adminService;

    private static ServletConfig _cfg;

    private static ServletContext _ctx;

    @Captor
    ArgumentCaptor<String> _stringCaptor_;

    private final static String unexpectedErrorText = ResourceBundle.getBundle("langs.i18n_en_US").getString("global.unexpectedError");

    @BeforeAll
    public static void globalInit(){
        _cfg = mock(ServletConfig.class);
        _ctx = mock(ServletContext.class);
        _adminService = mock(AdminService.class);

        lenient().when(_cfg.getServletContext()).thenReturn(_ctx);
        lenient().when(_ctx.getAttribute("admin_service")).thenReturn(_adminService);
        lenient().when(_ctx.getAttribute("gson")).thenReturn(TStore.getGson());
    }

    @BeforeEach
    public void init() throws IOException {
        lenient().when(_resp.getWriter()).thenReturn(_writer);

        HttpSession _session = mock(HttpSession.class);
        lenient().when(_req.getSession()).thenReturn(_session);
        lenient().when(_session.getAttribute("lang")).thenReturn("en_US");
    }

    @Nested
    @DisplayName("StatsController (doGet)")
    class statsControllerTests{
        private static StatsController statsController;

        @BeforeAll
        public static void beforeAll() throws ServletException {
            statsController = new StatsController();
            statsController.init(_cfg);
        }

        @Test
        public void successfulFlow() {
            assertDoesNotThrow(() -> statsController.doGet(_req, _resp));

            verify(_resp).setStatus(HttpServletResponse.SC_OK);
        }

        @Test
        @DisplayName("DBException [reaction]")
        public void reactOnDBException() throws DBException {
            doThrow(DBException.class).when(_adminService).getStats();

            assertDoesNotThrow(() -> statsController.doGet(_req, _resp));

            verify(_resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("CarController")
    class carControllerTests{
        private static CarController carController;

        @BeforeAll
        public static void beforeAll() throws ServletException {
            carController = new CarController();
            carController.init(_cfg);
        }

        @Nested
        class doGet{
            @Test
            public void successfulFlow() throws DescriptiveException {
                when(_req.getParameter("id")).thenReturn(CryptoStore.encrypt("1"));
                assertDoesNotThrow(() -> carController.doGet(_req, _resp));

                verify(_resp).setStatus(HttpServletResponse.SC_OK);
            }

            @Test
            @DisplayName("DE.CAR_IN_USE [reaction]")
            public void carInUseReaction() throws DBException, DescriptiveException {
                when(_req.getParameter("id")).thenReturn(CryptoStore.encrypt("1"));
                doThrow(new DescriptiveException(ExceptionReason.CAR_IN_USE)).when(_adminService).getCar(anyInt());

                assertDoesNotThrow(() -> carController.doGet(_req, _resp));

                verify(_resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                verify(_writer).write(_stringCaptor_.capture());

                assertEquals(unexpectedErrorText, _stringCaptor_.getValue());
            }

            @Test
            @DisplayName("DBException [reaction]")
            public void dbExceptionReaction() throws DBException, DescriptiveException {
                when(_req.getParameter("id")).thenReturn(CryptoStore.encrypt("1"));
                doThrow(new DescriptiveException(ExceptionReason.CAR_IN_USE)).when(_adminService).getCar(anyInt());

                assertDoesNotThrow(() -> carController.doGet(_req, _resp));

                verify(_resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                verify(_writer).write(_stringCaptor_.capture());

                assertEquals(unexpectedErrorText, _stringCaptor_.getValue());
            }
        }

        @Nested
        class doPost{
            @Test
            public void successfulFlow() throws ServletException, DBException, IOException, DescriptiveException {
                when(_adminService.createCar(_req)).thenReturn(new HashMap<>(Map.of("id", "1", "brand", "Mercedes", "model", "AMG")));

                assertDoesNotThrow(() -> carController.doPost(_req, _resp));

                verify(_resp).setStatus(HttpServletResponse.SC_OK);
            }

            @Test
            @DisplayName("VALIDATION_ERROR [reaction]")
            public void validationErrorReaction() throws DBException, DescriptiveException, ServletException, IOException {
                doThrow(new DescriptiveException(ExceptionReason.VALIDATION_ERROR)).when(_adminService).createCar(_req);

                assertDoesNotThrow(() -> carController.doPost(_req, _resp));

                verify(_resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                verify(_writer).write(_stringCaptor_.capture());

                assertNotEquals(unexpectedErrorText, _stringCaptor_.getValue());
            }

            @Test
            @DisplayName("BAD_VALUE [reaction]")
            public void badValueReaction() throws DBException, DescriptiveException, ServletException, IOException {
                doThrow(new DescriptiveException(ExceptionReason.BAD_VALUE)).when(_adminService).createCar(_req);

                assertDoesNotThrow(() -> carController.doPost(_req, _resp));

                verify(_resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                verify(_writer).write(_stringCaptor_.capture());

                assertNotEquals(unexpectedErrorText, _stringCaptor_.getValue());
            }

            @Test
            @DisplayName("DBException [reaction]")
            public void dbExceptionReaction() throws DBException, DescriptiveException, ServletException, IOException {
                doThrow(DBException.class).when(_adminService).createCar(_req);

                assertDoesNotThrow(() -> carController.doPost(_req, _resp));

                verify(_resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                verify(_writer).write(_stringCaptor_.capture());

                assertEquals(unexpectedErrorText, _stringCaptor_.getValue());
            }
        }

        @Nested
        class doPut{
            @Test
            public void successfulFlow() {
                assertDoesNotThrow(() -> carController.doPut(_req, _resp));

                verify(_resp).setStatus(HttpServletResponse.SC_OK);
            }

            @Test
            @DisplayName("VALIDATION_ERROR [reaction]")
            public void validationErrorReaction() throws DBException, DescriptiveException, ServletException, IOException {
                doThrow(new DescriptiveException(ExceptionReason.VALIDATION_ERROR)).when(_adminService).createCar(_req);

                assertDoesNotThrow(() -> carController.doPost(_req, _resp));

                verify(_resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                verify(_writer).write(_stringCaptor_.capture());

                assertNotEquals(unexpectedErrorText, _stringCaptor_.getValue());
            }

            @Test
            @DisplayName("DBException [reaction]")
            public void dbExceptionReaction() throws DBException, DescriptiveException, ServletException, IOException {
                doThrow(DBException.class).when(_adminService).createCar(_req);

                assertDoesNotThrow(() -> carController.doPost(_req, _resp));

                verify(_resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                verify(_writer).write(_stringCaptor_.capture());

                assertEquals(unexpectedErrorText, _stringCaptor_.getValue());
            }
        }

        @Nested
        class doDelete{
            @Test
            public void successfulFlow() {
                assertDoesNotThrow(() -> carController.doDelete(_req, _resp));

                verify(_resp).setStatus(HttpServletResponse.SC_OK);
            }

            @Test
            @DisplayName("CAR_IN_USE [reaction]")
            public void validationErrorReaction() throws DBException, DescriptiveException, ServletException, IOException {
                doThrow(new DescriptiveException(ExceptionReason.CAR_IN_USE)).when(_adminService).deleteCar(_req);

                assertDoesNotThrow(() -> carController.doDelete(_req, _resp));

                verify(_resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                verify(_writer).write(_stringCaptor_.capture());

                assertNotEquals(unexpectedErrorText, _stringCaptor_.getValue());
            }

            @Test
            @DisplayName("DBException [reaction]")
            public void dbExceptionReaction() throws DBException, DescriptiveException, ServletException, IOException {
                doThrow(DBException.class).when(_adminService).deleteCar(_req);

                assertDoesNotThrow(() -> carController.doDelete(_req, _resp));

                verify(_resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                verify(_writer).write(_stringCaptor_.capture());

                assertEquals(unexpectedErrorText, _stringCaptor_.getValue());
            }

            @Test
            @DisplayName("IOException [reaction]")
            public void ioExceptionReaction() throws DBException, DescriptiveException, ServletException, IOException {
                doThrow(IOException.class).when(_adminService).deleteCar(_req);

                assertDoesNotThrow(() -> carController.doDelete(_req, _resp));

                verify(_resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                verify(_writer).write(_stringCaptor_.capture());

                assertEquals(unexpectedErrorText, _stringCaptor_.getValue());
            }
        }
    }
}
