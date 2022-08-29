package com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.roles;

import com.github.DiachenkoMD.entities.enums.Roles;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.RoleGuard;

/**
 * A special case of {@link RoleGuard} to prevent access to any not-admin users.
 */
public class AdminRGuard extends RoleGuard{
    public AdminRGuard(){
        this.setRoles(Roles.ADMIN);
    }
}
