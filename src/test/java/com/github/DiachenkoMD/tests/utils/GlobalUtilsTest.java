package com.github.DiachenkoMD.tests.utils;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.JSi18n;
import com.github.DiachenkoMD.entities.dto.JWTAnalysis;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.github.DiachenkoMD.entities.enums.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static com.github.DiachenkoMD.web.utils.Utils.*;
import static org.mockito.Mockito.*;

import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.utils.TGenerators;
import com.github.DiachenkoMD.web.utils.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.fusionauth.jwt.domain.JWT;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@ExtendWith({MockitoExtension.class})
@DisplayName("Global utils")
public class GlobalUtilsTest {
    @Nested
    @DisplayName("capitalize")
    class CapitalizeTest{

        @Test
        @DisplayName("Correct capitalization")
        public void correctCapitalizationTest(){
            assertEquals(capitalize("love"), "Love");
        }

        @Test
        @DisplayName("Throw exception on null")
        public void incorrectCapitalizationTest(){
            assertThrows(IllegalArgumentException.class, () -> capitalize(null));
        }
    }

    @Nested
    @DisplayName("validate + Validatable")
    class validate{
        @Test
        @DisplayName("Validation pass")
        void validationPass(){
            Validatable nameVT = Validatable.of("Firstname", ValidationParameters.NAME);
            Validatable dateOfBirthVT = Validatable.of("2000-01-01", ValidationParameters.DATE_OF_BIRTH);
            Validatable dateOfIssueVT = Validatable.of("2010-01-01", ValidationParameters.DATE_OF_ISSUE);
            Validatable emailVT = Validatable.of("test@gmail.com", ValidationParameters.EMAIL);
            Validatable passwordVT = Validatable.of("nekear1234", ValidationParameters.PASSWORD);
            Validatable docNumberVT = Validatable.of("123456789", ValidationParameters.DOC_NUMBER);
            Validatable rntrcVT = Validatable.of("1234567890", ValidationParameters.RNTRC);
            Validatable authorityVT = Validatable.of("1234", ValidationParameters.AUTHORITY);

            assertTrue(
                    validate(
                        nameVT,
                        dateOfBirthVT,
                        dateOfIssueVT,
                        emailVT,
                        passwordVT,
                        docNumberVT,
                        rntrcVT,
                        authorityVT
                    )
            );
        }

        @ParameterizedTest
        @DisplayName("Validation fail")
        @MethodSource("provideInvalidParameters")
        void validationFail(Object value, ValidationParameters parameter){
            assertFalse(Validatable.of(value, parameter).validate());
        }

        private static Stream<Arguments> provideInvalidParameters() {
            return Stream.of(
                    Arguments.of("Bad123", ValidationParameters.NAME),
                    Arguments.of("1100-02-02", ValidationParameters.DATE_OF_BIRTH),
                    Arguments.of("1990-01-01", ValidationParameters.DATE_OF_ISSUE),
                    Arguments.of("mail.com", ValidationParameters.EMAIL),
                    Arguments.of("$%$#%", ValidationParameters.PASSWORD),
                    Arguments.of(1, ValidationParameters.DOC_NUMBER),
                    Arguments.of(1, ValidationParameters.RNTRC),
                    Arguments.of(1, ValidationParameters.AUTHORITY)
            );
        }

    }

    @Test
    @DisplayName("Password encryption tests")
    void passwordEncryptionTests(){
        String password = "hello1234";

        String encrypted = encryptPassword(password);

        assertNotNull(encrypted);
        assertNotEquals(password, encrypted);

        assertTrue(encryptedPasswordsCompare(password, encrypted));
    }

    @Test
    @DisplayName("cleanGetString")
    void cleanGetStringTests(){
        assertNull(cleanGetString(null));
        assertNull(cleanGetString("    "));
        assertEquals("hello", cleanGetString("   hello "));
    }

    @Test
    void generateRandomStringTest(){
        String generated = generateRandomString(12);

        assertNotNull(generated);
        assertEquals(12, generated.length());
    }

    @Test
    void emailNotifyTest(){
        assertDoesNotThrow(() -> emailNotify("FromJunitTest@crrt.com", "Junit test title", "Junit test body", true));
    }

    @Test
    void getCookieFromArrayTest(){
        Cookie cookie1 = new Cookie("cookie1", "value1");
        Cookie cookie2 = new Cookie("cookie2", "value2");
        Cookie cookie3 = new Cookie("cookie3", "value3");

        Optional<Cookie> foundCookie = getCookieFromArray("cookie2", new Cookie[]{cookie1, cookie2, cookie3});

        assertTrue(foundCookie.isPresent());
        assertEquals(cookie2, foundCookie.get());
    }

    @Test
    void createCookieTest(){
        Cookie cookie = createCookie("cookie1", "value1", "/tests/");

        assertNotNull(cookie);
        assertEquals("cookie1", cookie.getName());
        assertEquals("value1", cookie.getValue());
        assertEquals("/tests/", cookie.getPath());
    }

    @Test
    void getRoleTranslationTest(){
        assertEquals("roles."+Roles.CLIENT.keyword(), getRoleTranslation(Roles.CLIENT));
    }

    @Test
    void getLangTest(){
        HttpServletRequest _req = mock(HttpServletRequest.class);
        HttpSession _session = mock(HttpSession.class);
        when(_req.getSession()).thenReturn(_session);
        when(_session.getAttribute("lang")).thenReturn("uk");

        assertEquals("uk", getLang(_req));
    }

    @Test
    void sendSuccessTest() throws IOException {
        String data = "Hello world!";
        HttpServletResponse _resp = mock(HttpServletResponse.class);

        // Setting print writer to read data
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream));

        when(_resp.getWriter()).thenReturn(writer);

        assertDoesNotThrow(() -> sendSuccess(data, _resp));

        // Checks
        verify(_resp).setStatus(HttpServletResponse.SC_OK);
        assertEquals(data, outputStream.toString());
    }

    @Test
    void sendExceptionTest() throws IOException {
        String data = "Hello, cruel world...";
        HttpServletResponse _resp = mock(HttpServletResponse.class);

        // Setting print writer to read data
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream));

        when(_resp.getWriter()).thenReturn(writer);

        sendException(data, _resp);

        // Checks
        verify(_resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals(data, outputStream.toString());
    }

    @Test
    void cleanTest(){
        String incomingString = "  [Bad!    ";
        String expectedString = "![Bad!!";

        assertEquals(expectedString, clean(incomingString));
    }

    @Test
    void multieqTest(){
        assertTrue(multieq("Apple", "orange", "tomato", "APPle"));

        assertFalse(multieq("Word", "job", "NoThing"));

        Car car = TGenerators.genCar();
        Car car2 = TGenerators.genCar();
        Car car3 = TGenerators.genCar();

        System.out.println(car.hashCode());
        System.out.println(car2);
        System.out.println(car3);

        assertTrue(multieq(car, car3, car2, car));
        assertFalse(multieq(car, car3, car2));
    }

    @Test
    void getFileExtensionTest(){
        assertEquals(".jpg", getFileExtension("some.jpg"));
    }

    @Test
    void cryptoStoreTest(){
        String str = "Hello world!";

        String encrypted = assertDoesNotThrow(() -> CryptoStore.encrypt(str));
        assertNotEquals(str, encrypted);

        String decrypted = assertDoesNotThrow(() -> CryptoStore.decrypt(encrypted));
        assertEquals(str, decrypted);
    }

    @Test
    void emailFormatterTest(){
        String title = "Hello title!";
        String body = "Hello body!";

        assertThat(EmailFormatter.format(title, body).length()).isGreaterThanOrEqualTo(title.length()+body.length());
    }

    @Test
    @DisplayName("JSJS and JSi18n tests")
    void JSJSAndJSi18nTests(){
        String lang = "en_US";
        Gson gson = new Gson();

        // Values, expected to be acquired from method, which purpose is to convert enums to json objects
        int citiesListExp = Cities.values().length;
        int segmentsListExp = CarSegments.values().length;
        int rolesListExp = Roles.values().length;
        int accountStatesExp = AccountStates.values().length;
        int invoiceStatusesFullExp = InvoiceStatuses.values().length;
        int invoiceStatusesPartlyExp = InvoiceStatuses.values().length-1;

        // Types for gson deserialization
        Type listOfStrings = new TypeToken<List<String>>() {}.getType();
        Type mapOfStringArray = new TypeToken<Map<String, List<String>>>() {}.getType();
        Type mapOfIntegerMap = new TypeToken<Map<Integer, Map<String, String>>>() {}.getType();
        Type mapOfStringString = new TypeToken<Map<String, String>>() {}.getType();


        // transForLoginPage()
        String transForLoginPageStr = assertDoesNotThrow(() -> JSJS.transForLoginPage(lang));
        JSi18n transForLoginPageObj = gson.fromJson(transForLoginPageStr, JSi18n.class);
        assertThat(transForLoginPageObj.notiflix.size()).isGreaterThanOrEqualTo(1);
        assertThat(transForLoginPageObj.inputs.size()).isGreaterThanOrEqualTo(2);

        // transForRegisterPage()
        String transForRegisterPageStr = assertDoesNotThrow(() -> JSJS.transForRegisterPage(lang));
        JSi18n transForRegisterPageObj = gson.fromJson(transForRegisterPageStr, JSi18n.class);
        assertThat(transForRegisterPageObj.notiflix).isNull();
        assertThat(transForRegisterPageObj.inputs.size()).isGreaterThanOrEqualTo(5);

        // transForPassport()
        String transForPassportStr = assertDoesNotThrow(() -> JSJS.transForPassport(lang));
        JSi18n transForPassportObj = gson.fromJson(transForPassportStr, JSi18n.class);
        assertThat(transForPassportObj.notiflix).isNull();
        assertThat(transForPassportObj.inputs.size()).isGreaterThanOrEqualTo(8);

        // transForDatepicker()
        String transForDatePickerStr = assertDoesNotThrow(() -> JSJS.transForDatepicker(lang));
        Map<String, List<String>> transForDatePickerMap = gson.fromJson(transForDatePickerStr, mapOfStringArray);
        assertThat(transForDatePickerMap.size()).isGreaterThanOrEqualTo(4);

        // Method for converting enums to json
        List<Map.Entry<Integer, Supplier<String>>> enumTranslations = List.of(
                Map.entry(citiesListExp, () -> JSJS.CitiesList(lang)),
                Map.entry(segmentsListExp, () -> JSJS.SegmentsList(lang)),
                Map.entry(rolesListExp, () -> JSJS.RolesList(lang)),
                Map.entry(accountStatesExp, () -> JSJS.AccountStatesList(lang)),
                Map.entry(invoiceStatusesFullExp, () -> JSJS.InvoiceStatusesList(lang, true)),
                Map.entry(invoiceStatusesPartlyExp, () -> JSJS.InvoiceStatusesList(lang, false))
        );

        enumTranslations.forEach(
                x -> assertThat(((Map<Integer, Map<String, String>>) gson.fromJson(x.getValue().get(), mapOfIntegerMap)).size())
                        .isGreaterThanOrEqualTo(x.getKey())
        );

        // transForUsersDeleteConfirmation()
        String transForUsersDeleteConfirmationStr = assertDoesNotThrow(() -> JSJS.transForUsersDeleteConfirmation(lang));
        Map<String, String> transForUsersDeleteConfirmationMap = gson.fromJson(transForUsersDeleteConfirmationStr, mapOfStringString);

        assertThat(transForUsersDeleteConfirmationMap).containsKeys("title", "desc", "yes", "no");
    }


    @Nested
    @DisplayName("Passport")
    class passportTests{

        @ParameterizedTest(name = "{index}")
        @DisplayName("Validation fail")
        @MethodSource("provideInvalidParameters")
        void validationFail(Supplier<Passport> passportSupplier ){
            Passport passport = passportSupplier.get();
            DescriptiveException descExc = assertThrows(DescriptiveException.class, passport::validate);
            assertEquals(ExceptionReason.PASSPORT_VALIDATION_ERROR, descExc.getReason());
        }

        private static Stream<Arguments> provideInvalidParameters() {
            return Stream.of(
                    Arguments.of(
                            (Supplier<Passport>) () -> {
                                    Passport passport = TGenerators.genPassport();
                                    passport.setFirstname("Name123");
                                    return passport;
                            }
                    ),
                    Arguments.of(
                            (Supplier<Passport>) () -> {
                                Passport passport = TGenerators.genPassport();
                                passport.setSurname("Name123");
                                return passport;
                            }
                    ),
                    Arguments.of(
                            (Supplier<Passport>) () -> {
                                Passport passport = TGenerators.genPassport();
                                passport.setPatronymic("Name123");
                                return passport;
                            }
                    ),
                    Arguments.of(
                            (Supplier<Passport>) () -> {
                                Passport passport = TGenerators.genPassport();
                                passport.setDateOfBirth(LocalDate.of(1000, 1, 1));
                                return passport;
                            }
                    ),
                    Arguments.of(
                            (Supplier<Passport>) () -> {
                                Passport passport = TGenerators.genPassport();
                                passport.setDateOfIssue(LocalDate.of(1990, 1, 1));
                                return passport;
                            }
                    ),
                    Arguments.of(
                            (Supplier<Passport>) () -> {
                                Passport passport = TGenerators.genPassport();
                                passport.setDocNumber(1);
                                return passport;
                            }
                    ),
                    Arguments.of(
                            (Supplier<Passport>) () -> {
                                Passport passport = TGenerators.genPassport();
                                passport.setRntrc(1);
                                return passport;
                            }
                    ),
                    Arguments.of(
                            (Supplier<Passport>) () -> {
                                Passport passport = TGenerators.genPassport();
                                passport.setAuthority(1);
                                return passport;
                            }
                    )
            );
        }
        @Test
        @DisplayName("Passport user is too young [fail]")
        void userIsTooYoungFail(){
            Passport passport = TGenerators.genPassport();
            passport.setDateOfBirth(LocalDate.now().minusYears(6));

            DescriptiveException descExc = assertThrows(DescriptiveException.class, passport::validate);

            assertEquals(ExceptionReason.AGE_TOO_YOUNG, descExc.getReason());
        }

        @Test
        @DisplayName("Passport must be up to date [fail]")
        void passportMustBeUpToDateFail(){
            Passport passport = TGenerators.genPassport();
            passport.setDateOfIssue(LocalDate.now().minusYears(12));

            DescriptiveException descExc = assertThrows(DescriptiveException.class, passport::validate);

            assertEquals(ExceptionReason.PASSPORT_BAD_DATE_ISSUE, descExc.getReason());
        }

        @Test
        @DisplayName("Validation successful")
        void validationSuccessful(){
            Passport passport = TGenerators.genPassport();
            assertDoesNotThrow(passport::validate);
        }
    }

    @Test
    @DisplayName("JWTManager")
    void JWTManagerTest(){

        Map<String, String> originalMap = Map.of(
                "key1", "value1",
                "key2", "value2"
        );

        String encoded = JWTManager.encode(originalMap);
        JWT decoded = JWTManager.decode(encoded);

        assertEquals(originalMap.get("key1"), decoded.getString("key1"));
        assertEquals(originalMap.get("key2"), decoded.getString("key2"));
    }

    @Nested
    @DisplayName("JWTAnalysis")
    class JWTAnalysisTests{

        private Map<String, String> originalMap;

        @BeforeEach
        public void beforeEach(){
            this.originalMap = Map.of(
                    "key1", "value1",
                    "key2", "value2"
            );
        }

        @Test
        void goodToken(){
            String encoded = JWTManager.encode(originalMap);

            JWTAnalysis analysis = JWTAnalysis.of(encoded);

            assertEquals(originalMap.get("key1"), analysis.getToken().getString("key1"));
            assertEquals(originalMap.get("key2"), analysis.getToken().getString("key2"));
            assertTrue(analysis.isValid());
        }

        @Test
        void expiredToken(){
            String encoded = JWTManager.encode(originalMap, LocalDateTime.now().minusDays(1));

            JWTAnalysis analysis = JWTAnalysis.of(encoded);

            assertTrue(analysis.isExpired());
            assertFalse(analysis.isValid());
        }

        @Test
        void badSignToken(){
            String encoded = JWTManager.encode(originalMap, LocalDateTime.now().minusDays(1));

            JWTAnalysis analysis = JWTAnalysis.of(encoded.replace("a", "b"));

            assertTrue(analysis.isInvalidSigned());
            assertFalse(analysis.isValid());
        }

        @Test
        void disabledToken(){
            String encoded = JWTManager.encode(originalMap);

            JWTManager.disableToken(JWTManager.decode(encoded));

            JWTAnalysis analysis = JWTAnalysis.of(encoded);

            assertTrue(analysis.isDisabled());
            assertFalse(analysis.isValid());

            JWTManager.emptyDisabledTokensList();
        }

        @Test
        void badFormattedToken(){
            JWTAnalysis analysis = JWTAnalysis.of("sadfasf");

            assertTrue(analysis.isBadFormatted());
            assertFalse(analysis.isValid());
        }
    }
}
