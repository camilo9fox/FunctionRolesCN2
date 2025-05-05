package com.function.events;

import com.function.model.Rol;
import java.util.List;

public class RolesRetrievedEvent {
    private List<Rol> roles;

    public RolesRetrievedEvent(List<Rol> roles) {
        this.roles = roles;
    }

    public List<Rol> getRoles() {
        return roles;
    }

    public void setRoles(List<Rol> roles) {
        this.roles = roles;
    }
}
