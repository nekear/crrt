package com.github.DiachenkoMD.exceptions;

/**
 * Exception reasons used at {@link DescriptiveException DescriptiveException}
 */
public enum ExceptionReason {
    VALIDATION_ERROR,
    EMAIL_EXISTS,
//  Confirmation codes
    CONFIRMATION_CODE_ERROR,

    CONFIRMATION_NO_SUCH_CODE,
    CONFIRMATION_CODE_EMPTY,
    CONFIRMATION_PROCESS_ERROR
}
