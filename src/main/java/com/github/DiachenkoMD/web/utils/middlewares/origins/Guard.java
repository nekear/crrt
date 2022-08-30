package com.github.DiachenkoMD.web.utils.middlewares.origins;

import com.github.DiachenkoMD.web.utils.middlewares.guardian.GuardingTypes;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Acts more like marker class for guards. Guards protect routes executing different checks. And redirecting / forwarding if needed. <br/>
 * All children of that class can be passed to {@link com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards @UseGuards}.
 */
public abstract non-sealed class Guard extends Chainable {
    private final static Logger logger = LogManager.getLogger(Guard.class);

    /**
     * Method for getting guardian type. Guardian type can be PAGE or API (taken from {@link GuardingTypes}).
     * It acts like marker for Guard implementations and says whether Guard impl should redirect (if servlet is PAGE) or just send pretty exception message (if request came from API).
     * @param req
     */
    public static GuardingTypes getGuardianType(HttpServletRequest req){
        Object guardianTypeObj = req.getAttribute("guardingType");

        if(guardianTypeObj == null)
            return GuardingTypes.API;

        return (GuardingTypes) guardianTypeObj;
    }
}
