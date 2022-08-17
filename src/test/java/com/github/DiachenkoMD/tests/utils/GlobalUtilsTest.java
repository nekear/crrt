package com.github.DiachenkoMD.tests.utils;

import com.github.DiachenkoMD.entities.adapters.DBCoupledAdapter;
import com.github.DiachenkoMD.entities.adapters.LocalDateAdapter;
import com.github.DiachenkoMD.entities.adapters.LocalDateTimeAdapter;
import com.github.DiachenkoMD.entities.adapters.Skip;
import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.github.DiachenkoMD.entities.enums.*;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import static org.junit.jupiter.api.Assertions.*;

import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.Validatable;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Stream;
@ExtendWith({MockitoExtension.class})
@DisplayName("Global utils")
public class GlobalUtilsTest {

    @Nested
    @DisplayName("flatJsonParser")
    class FlatJsonParserTest{
        @Test
        @DisplayName("Contains only present fields")
        public void testWithPresentOnly(){
            String testingJsonObject = "{\n" +
                    "    \"email\": \"some@gmail.com\",\n" +
                    "    \"firstname\": \"user1\"\n" +
                    "}";

            AuthUser expectedUser = new AuthUser();
            expectedUser.setEmail("some@gmail.com");
            expectedUser.setFirstname("user1");

            assertTrue(Utils.reflectiveEquals(Utils.flatJsonParser(testingJsonObject, AuthUser.class), expectedUser));
        }

        @Test
        @DisplayName("Contains present and not")
        public void testWithPresentAndNot(){
            String testingJsonObject = "{\n" +
                    "    \"email\": \"some2@gmail.com\",\n" +
                    "    \"firstname\": \"user2\",\n" +
                    "    \"notPresentField1\": \"11.11.11\"\n" +
                    "}";

            AuthUser expectedUser = new AuthUser();
            expectedUser.setEmail("some2@gmail.com");
            expectedUser.setFirstname("user2");

            assertTrue(Utils.reflectiveEquals(Utils.flatJsonParser(testingJsonObject, AuthUser.class), expectedUser));
        }

        @Test
        @DisplayName("Contains fields not present")
        public void testWithFieldsNotPresent(){
            String testingJsonObject = "{\n" +
                    "    \"notPresentField1\": \"notPresentContent\",\n" +
                    "    \"notPresentField2\": 1" +
                    "}";

            assertNull(Utils.flatJsonParser(testingJsonObject, AuthUser.class));
        }
    }



    @Nested
    @DisplayName("reflectiveEquals")
    class ReflectiveEqualsTest{
        @ParameterizedTest
        @MethodSource("provideTrueParameters")
        public void expectedTrue(Object u1, Object u2){
            assertTrue(Utils.reflectiveEquals(u1, u2));
        }

        @ParameterizedTest
        @MethodSource("provideFalseParameters")
        public void expectedFalse(Object u1, Object u2){
            assertFalse(Utils.reflectiveEquals(u1, u2));
        }

        private static Stream<Arguments> provideTrueParameters() {
            return Stream.of(
                    Arguments.of(
                            AuthUser.of(null, "John", null, null),
                            AuthUser.of(null, "John", null, null)
                    ),
                    Arguments.of(
                            AuthUser.of("martin@gmail.com", null, null, "martevich"),
                            AuthUser.of("martin@gmail.com", null, null, "martevich")
                    ),
                    Arguments.of(
                            AuthUser.of(null, null, null, null),
                            AuthUser.of(null, null, null, null)
                    ),
                    Arguments.of(
                            AuthUser.of(null, null, null, null, null, Roles.ADMIN, null, 0, null),
                            AuthUser.of(null, null, null, null, null, Roles.ADMIN, null, 0, null)
                    ),
                    Arguments.of(
                            new ForComparing(1, 0),
                            new ForComparing(1, 0)
                    ),
                    Arguments.of(
                            new ForComparing(0, 2D),
                            new ForComparing(0, 2D)
                    )
            );
        }

        private static Stream<Arguments> provideFalseParameters() {
            return Stream.of(
                    Arguments.of(
                            AuthUser.of(null, "John", null, null),
                            AuthUser.of(null, "John2", null, null)
                    ),
                    Arguments.of(
                            AuthUser.of("martin@gmail.com", "Luther", "King", "martevich"),
                            AuthUser.of("martin@gmail.com", null, null, "martevich")
                    ),
                    Arguments.of(
                            AuthUser.of("somecryptedid", null, null, null, null, null, null, 0, null),
                            AuthUser.of(null, null, null, null)
                    ),
                    Arguments.of(
                            AuthUser.of(null, null, null, null, null, Roles.CLIENT, null, 0, null),
                            AuthUser.of(null, null, null, null, null, Roles.ADMIN, null, 0, null)
                    ),
                    Arguments.of(
                            new ForComparing(0, 10.0),
                            new ForComparing(0, 11.0)
                    ),
                    Arguments.of(
                            new ForComparing(1, 0),
                            new ForComparing(0, 0)
                    )

            );
        }

        private static class ForComparing{
            int simpleInt;
            double simpleFloat;

            ForComparing(int simpleInt, double simpleFloat){
                this.simpleInt = simpleInt;
                this.simpleFloat = simpleFloat;
            }
        }
    }

    @Nested
    @DisplayName("capitalize")
    class CapitalizeTest{

        @Test
        @DisplayName("Correct capitalization")
        public void correctCapitalizationTest(){
            assertEquals(Utils.capitalize("love"), "Love");
        }

        @Test
        @DisplayName("Throw exception on null")
        public void incorrectCapitalizationTest(){
            assertThrows(IllegalArgumentException.class, () -> Utils.capitalize(null));
        }
    }

    // TODO: REMOVE ON PRODUCTION
    @Test
    @DisplayName("Testing password protection")
    public void testPasswordProtection(){
        String myPassword = "helloworld1234";
        String encrypted = Utils.encryptPassword(myPassword);

        System.out.println("Encrypted: " + encrypted);

        assertTrue(Utils.encryptedPasswordsCompare(myPassword, encrypted));
    }

    @Test
    @DisplayName("Testing validation on cyrillic")
    public void testValidation(){
        assertTrue(Utils.validate("Привет", ValidationParameters.NAME));
    }

    @Captor
    private ArgumentCaptor<DescriptiveException> argumentCaptor;

    @Test
    public void testGenerateKey() throws Exception {
        String input = "1";
        String cipherText = CryptoStore.encrypt(input);
        String plainText = CryptoStore.decrypt(cipherText);

        System.out.println(cipherText);
        System.out.println(plainText);
        assertEquals(input, plainText);
    }

    @Test
    public void testUserEncryptDecrypt() throws Exception {
        AuthUser user = new AuthUser();
        user.setId(1);

        System.out.println("--> " + user.encrypt());

        System.out.println("User:" + user);

        String encryptedId = CryptoStore.encrypt("1");

        assertEquals(user.getId().toString(), encryptedId);

        user.decrypt();

        System.out.println(user);

        assertEquals(user.getId().toString(), CryptoStore.decrypt(encryptedId));
    }
    @Test
    public void testSegments(){
        System.out.println(CarSegments.D_SEGMENT);
    }

    @Test
    public void testAdapters(){
        Car car = new Car();

        car.setId("1");
        car.setModel("Mercedes");
        car.setBrand("G117");
        car.setSegment(CarSegments.D_SEGMENT);
        car.setCity(Cities.LVIV);
        car.setPrice(3000d);

        String jsoned = new Gson().toJson(car);

        System.out.println(jsoned);

        System.out.println(new Gson().fromJson(jsoned, Car.class));
    }

    @Test
    public void getFileExtensionTest(){
        String fileName = "hello.jpg";

        System.out.println(Utils.getFileExtension("Frame 1 (2).jpg"));

        assertEquals(".jpg", Utils.getFileExtension(fileName));
    }

    @Test
    public void validatableTest(){
        Validatable birth = Validatable.of(LocalDate.of(2014, Month.JANUARY, 1), ValidationParameters.DATE_OF_BIRTH);
        Validatable name = Validatable.of("Vaisl", ValidationParameters.NAME);
        Validatable nameFail = Validatable.of("hf123", ValidationParameters.NAME);
        Validatable email = Validatable.of("xpert@gmail.com", ValidationParameters.EMAIL);

        assertTrue(birth.validate());
        assertTrue(name.validate());
        assertFalse(nameFail.validate());
        assertTrue(email.validate());

        assertTrue(Utils.validate(birth, name, email));
        assertFalse(Utils.validate(birth, name, nameFail, email));

        assertTrue(Utils.validate(List.of(birth, name, email)));
    }
    @Test
    public void validatableTest2() throws DescriptiveException {
        String json = "{\"firstname\":\"asdfasfd\",\"surname\":\"asdfasdf\",\"patronymic\":\"asdfasf\",\"date_of_birth\":\"2003-10-22\",\"date_of_issue\":\"2004-10-22\",\"doc_number\":324123434,\"rntrc\":5555555555,\"authority\":6666}";

        Gson gson = new GsonBuilder()
                .addSerializationExclusionStrategy(new ExclusionStrategy()
                {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f)
                    {
                        return f.getAnnotation(Skip.class) != null;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz)
                    {
                        return false;
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeHierarchyAdapter(DBCoupled.class, new DBCoupledAdapter())
                .create();

        Passport passport = gson.fromJson(json, Passport.class);

        passport.validate();
    }
}
