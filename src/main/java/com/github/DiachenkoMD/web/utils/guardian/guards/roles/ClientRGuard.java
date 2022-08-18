package com.github.DiachenkoMD.web.utils.guardian.guards.roles;

import com.github.DiachenkoMD.entities.enums.Roles;
import com.github.DiachenkoMD.web.utils.guardian.guards.RoleGuard;

/**
 * A special case of {@link RoleGuard} to prevent access to any not-admin users.
 */
public class ClientRGuard extends RoleGuard{
    public ClientRGuard(){
        this.setRole(Roles.CLIENT);
    }
}