package com.github.DiachenkoMD.tests.utils;

import com.github.DiachenkoMD.dto.Roles;
import com.github.DiachenkoMD.dto.User;
import static org.junit.jupiter.api.Assertions.*;

import com.github.DiachenkoMD.utils.Utils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

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
                    "    \"username\": \"user1\"\n" +
                    "}";

            User expectedUser = new User();
            expectedUser.setEmail("some@gmail.com");
            expectedUser.setUsername("user1");

            assertTrue(Utils.reflectiveEquals(Utils.flatJsonParser(testingJsonObject, User.class), expectedUser));
        }

        @Test
        @DisplayName("Contains present and not")
        public void testWithPresentAndNot(){
            String testingJsonObject = "{\n" +
                    "    \"email\": \"some2@gmail.com\",\n" +
                    "    \"username\": \"user2\",\n" +
                    "    \"notPresentField1\": \"11.11.11\"\n" +
                    "}";

            User expectedUser = new User();
            expectedUser.setEmail("some2@gmail.com");
            expectedUser.setUsername("user2");

            assertTrue(Utils.reflectiveEquals(Utils.flatJsonParser(testingJsonObject, User.class), expectedUser));
        }

        @Test
        @DisplayName("Contains fields not present")
        public void testWithFieldsNotPresent(){
            String testingJsonObject = "{\n" +
                    "    \"notPresentField1\": \"notPresentContent\",\n" +
                    "    \"notPresentField2\": 1" +
                    "}";

            assertNull(Utils.flatJsonParser(testingJsonObject, User.class));
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
                            new User(null, "John", null, null),
                            new User(null, "John", null, null)
                    ),
                    Arguments.of(
                            new User("martin@gmail.com", null, null, "martevich"),
                            new User("martin@gmail.com", null, null, "martevich")
                    ),
                    Arguments.of(
                            new User(null, null, null, null),
                            new User(null, null, null, null)
                    ),
                    Arguments.of(
                            new User(null, null, null, null, null, Roles.ADMIN, null),
                            new User(null, null, null, null, null, Roles.ADMIN, null)
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
                            new User(null, "John", null, null),
                            new User(null, "John2", null, null)
                    ),
                    Arguments.of(
                            new User("martin@gmail.com", "Luther", "King", "martevich"),
                            new User("martin@gmail.com", null, null, "martevich")
                    ),
                    Arguments.of(
                            new User("somecryptedid", null, null, null, null, null, null),
                            new User(null, null, null, null)
                    ),
                    Arguments.of(
                            new User(null, null, null, null, null, Roles.DEFAULT, null),
                            new User(null, null, null, null, null, Roles.ADMIN, null)
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
}
