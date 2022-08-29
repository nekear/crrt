package com.github.DiachenkoMD.web.utils.middlewares.origins;

import com.github.DiachenkoMD.web.utils.middlewares.guardian.GuardingTypes;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract non-sealed class Guard extends Chainable {
    private final static Logger logger = LogManager.getLogger(Guard.class);

    public static GuardingTypes getGuardianType(HttpServletRequest req){
        Object guardianTypeObj = req.getAttribute("guardingType");

        if(guardianTypeObj == null)
            return GuardingTypes.API;

        return (GuardingTypes) guardianTypeObj;
    }
}
