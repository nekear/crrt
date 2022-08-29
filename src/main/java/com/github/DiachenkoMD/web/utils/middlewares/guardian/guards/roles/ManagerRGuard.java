package com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.roles;

import com.github.DiachenkoMD.entities.enums.Roles;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.RoleGuard;

public class ManagerRGuard extends RoleGuard {
    public ManagerRGuard(){
        this.setRoles(Roles.MANAGER);
    }
}
