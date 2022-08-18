package com.github.DiachenkoMD.web.utils.guardian.guards.roles;

import com.github.DiachenkoMD.entities.enums.Roles;
import com.github.DiachenkoMD.web.utils.guardian.guards.RoleGuard;

public class ManagerRGuard extends RoleGuard {
    public ManagerRGuard(){
        this.setRole(Roles.MANAGER);
    }
}
