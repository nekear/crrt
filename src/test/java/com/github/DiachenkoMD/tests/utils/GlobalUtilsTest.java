package com.github.DiachenkoMD.tests.utils;

import com.github.DiachenkoMD.dto.Roles;
import com.github.DiachenkoMD.dto.User;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.DiachenkoMD.dto.ValidationParameters;
import com.github.DiachenkoMD.exceptions.DescriptiveException;
import com.github.DiachenkoMD.exceptions.ExceptionReason;
import com.github.DiachenkoMD.utils.Utils;
import com.github.DiachenkoMD.utils.flow_notifier.FlowNotifier;
import com.github.DiachenkoMD.utils.flow_notifier.Listener;
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

            User expectedUser = new User();
            expectedUser.setEmail("some@gmail.com");
            expectedUser.setFirstname("user1");

            assertTrue(Utils.reflectiveEquals(Utils.flatJsonParser(testingJsonObject, User.class), expectedUser));
        }

        @Test
        @DisplayName("Contains present and not")
        public void testWithPresentAndNot(){
            String testingJsonObject = "{\n" +
                    "    \"email\": \"some2@gmail.com\",\n" +
                    "    \"firstname\": \"user2\",\n" +
                    "    \"notPresentField1\": \"11.11.11\"\n" +
                    "}";

            User expectedUser = new User();
            expectedUser.setEmail("some2@gmail.com");
            expectedUser.setFirstname("user2");

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
                            new User(null, null, null, null, null, Roles.ADMIN, null, 0, null, null),
                            new User(null, null, null, null, null, Roles.ADMIN, null, 0, null, null)
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
                            new User("somecryptedid", null, null, null, null, null, null, 0, null, null),
                            new User(null, null, null, null)
                    ),
                    Arguments.of(
                            new User(null, null, null, null, null, Roles.DEFAULT, null, 0, null, null),
                            new User(null, null, null, null, null, Roles.ADMIN, null, 0, null, null)
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
        String encrypted = Utils.encrypt(myPassword);

        System.out.println("Encrypted: " + encrypted);

        assertTrue(Utils.encryptedCompare(myPassword, encrypted));
    }

    @Test
    @DisplayName("Testing validation on cyrillic")
    public void testValidation(){
        assertTrue(Utils.validate("Привет", ValidationParameters.NAME));
    }

    @Captor
    private ArgumentCaptor<DescriptiveException> argumentCaptor;
    @Test
    public void testFlowNotifier(){
        FlowNotifier fn = new FlowNotifier();

        Listener<DescriptiveException> _listener = mock(Listener.class);

        fn.addListener(DescriptiveException.class, _listener);


        fn.omit_e(new DescriptiveException("Fucking validationg failed", ExceptionReason.VALIDATION_ERROR));

        verify(_listener).ping(argumentCaptor.capture());

        assertEquals(argumentCaptor.getValue().getReason(), ExceptionReason.VALIDATION_ERROR);
    }
}
