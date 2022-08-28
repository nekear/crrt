package com.github.DiachenkoMD.web.utils.guardian.guards;

import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.guardian.GuardingTypes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.github.DiachenkoMD.entities.Constants.SESSION_AUTH;

public class AuthGuard extends Guard {
    private final static Logger logger = LogManager.getLogger(AuthGuard.class);

    @Override
    public boolean check(HttpServletRequest req, HttpServletResponse resp) {
        try{
            if(req.getSession().getAttribute(SESSION_AUTH) == null){
                GuardingTypes type = getGuardianType(req);

                if(type == GuardingTypes.PAGE) {
                    resp.sendRedirect(req.getContextPath());
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
