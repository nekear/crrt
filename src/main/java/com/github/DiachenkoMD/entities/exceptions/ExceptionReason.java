package com.github.DiachenkoMD.entities.exceptions;

/**
 * Exception reasons used at {@link DescriptiveException DescriptiveException}
 */
public enum ExceptionReason {
    BAD_FILE_EXTENSION,
    TOO_BIG_FILE_SIZE,
    TOO_MANY_FILES,
    ACQUIRING_ERROR,
    CRYPTO_OPERATION_ERROR,
    DB_ACTION_ERROR,
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
    LOGIN_WRONG_PASSWORD,
//  Profile
    UUD_FAILED_TO_UPDATE,
    UUD_PASSWORDS_DONT_MATCH,
    UNABLE_TO_SAVE_FILE,
    UNABLE_TO_DELETE_FILE
}