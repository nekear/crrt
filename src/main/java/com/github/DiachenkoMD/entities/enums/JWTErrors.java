package com.github.DiachenkoMD.entities.enums;

/**
 * JWT errors classification that may occur.
 */
public enum JWTErrors {
    TOKEN_EXPIRED,
    TOKEN_DISABLED,
    TOKEN_INVALID_SIGNATURE,
    TOKEN_BAD_FORMAT
}
