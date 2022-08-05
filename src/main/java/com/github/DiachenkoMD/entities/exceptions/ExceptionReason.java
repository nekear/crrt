package com.github.DiachenkoMD.entities.exceptions;

/**
 * Exception reasons used at {@link DescriptiveException DescriptiveException}
 */
public enum ExceptionReason {
    VALIDATION_ERROR,
    EMAIL_EXISTS,
    REGISTRATION_PROCESS_ERROR,
//  Confirmation codes
    CONFIRMATION_CODE_ERROR,
    CONFIRMATION_NO_SUCH_CODE,
    CONFIRMATION_CODE_EMPTY,
    CONFIRMATION_PROCESS_ERROR,
//    Login
    LOGIN_USER_NOT_FOUND,
    LOGIN_NOT_CONFIRMED,
    LOGIN_WRONG_PASSWORD
}
