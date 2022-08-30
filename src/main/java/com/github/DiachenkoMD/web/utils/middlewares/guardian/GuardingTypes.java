package com.github.DiachenkoMD.web.utils.middlewares.guardian;

/**
 * This enum`s values act like marker for Guard implementations and says whether Guard impl should redirect (if servlet is PAGE) or just send pretty exception message (if request came from API)
 */
public enum GuardingTypes {
    API,
    PAGE
}
