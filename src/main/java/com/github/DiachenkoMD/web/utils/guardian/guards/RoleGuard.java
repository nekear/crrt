package com.github.DiachenkoMD.web.utils.guardian.guards;

import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.enums.Roles;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.guardian.GuardingTypes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * RoleGuard class for allow guarding by roles. Should go after AuthGuard, because inside uses session "auth" attribute.
 */
public class RoleGuard extends Guard{
    private final static Logger logger = LogManager.getLogger(RoleGuard.class);

    private Roles role;

    @Override
    public boolean check(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthUser user = (AuthUser) req.getSession().getAttribute("auth");

        if(user.getRole() != role){
            GuardingTypes type = getGuardianType(req);

            if(type == GuardingTypes.PAGE) {
                resp.sendRedirect("profile");
            }else{
                Utils.sendException("Access denied!", resp);
            }
            return false;
        }

        return devolve(req, resp);
    }

    protected void setRole(Roles role){
        this.role = role;
    }
}
