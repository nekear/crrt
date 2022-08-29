package com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.roles;

import com.github.DiachenkoMD.entities.enums.Roles;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.RoleGuard;

public class DriverRGuard extends RoleGuard {
    public DriverRGuard(){
        this.setRoles(Roles.DRIVER);
    }
}
