package com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.roles;

import com.github.DiachenkoMD.entities.enums.Roles;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.RoleGuard;

/**
 * A special case of {@link RoleGuard} to prevent access to any users who are not admin or manager.<br/>
 * Simply put, combines admin and managers guards.
 */
public class HTierRGuard extends RoleGuard{
    public HTierRGuard(){
        this.setRoles(Roles.ADMIN, Roles.MANAGER);
    }
}
