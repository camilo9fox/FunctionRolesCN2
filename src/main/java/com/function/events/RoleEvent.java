package com.function.events;

import com.function.model.Rol;

public class RoleEvent {
    private Rol role;
    private Long roleId;

    public RoleEvent(Rol role) {
        this.role = role;
    }

    public RoleEvent(Long roleId) {
        this.roleId = roleId;
    }

    public Rol getRole() {
        return role;
    }

    public void setRole(Rol role) {
        this.role = role;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
