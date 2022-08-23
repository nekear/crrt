package com.github.DiachenkoMD.web.utils.guardian.guards;

import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.enums.AccountStates;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.guardian.GuardingTypes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StateGuard extends Guard {
    private final static Logger logger = LogManager.getLogger(StateGuard.class);

    @Override
    public boolean check(HttpServletRequest req, HttpServletResponse resp) {
        try{
            AuthUser user = (AuthUser) req.getSession().getAttribute("auth");

            if(user.getState() == AccountStates.BLOCKED){
                GuardingTypes type = getGuardianType(req);

                if(type == GuardingTypes.PAGE) {
                    req.getRequestDispatcher("/views/account_blocked.jsp").forward(req, resp);
                }else{
                    Utils.sendException("Access denied!", resp);
                }

                return false;
            }

            return devolve(req, resp);
        }catch (Exception e){
            logger.error(e);
            return false;
        }
    }

}
