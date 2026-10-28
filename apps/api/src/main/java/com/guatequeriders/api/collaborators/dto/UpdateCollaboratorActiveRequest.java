package com.guatequeriders.api.collaborators.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateCollaboratorActiveRequest {

    @NotNull(message = "Active is required")
    private Boolean active;

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
