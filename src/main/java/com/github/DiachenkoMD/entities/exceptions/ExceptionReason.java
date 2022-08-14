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
    UNABLE_TO_DELETE_FILE,

//  Admin error
    BAD_VALUE,
    IMAGE_NOT_FOUND_IN_DB,
    CAR_IN_USE,

// Managers error
    INVOICE_ALREADY_CANCELLED,
    INVOICE_ALREADY_REJECTED,
    INVOICE_ALREADY_STARTED,
    INVOICE_ALREADY_EXPIRED,
    REP_INVOICE_EXPIRATION_SHOULD_BE_LATER,
// Client panel
    REP_INVOICE_WAS_NOT_FOUND,
    NOT_ENOUGH_MONEY,
    REP_INVOICE_IS_ALREADY_PAID
}
