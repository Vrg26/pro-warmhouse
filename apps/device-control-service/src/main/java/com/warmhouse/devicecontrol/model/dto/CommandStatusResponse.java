package com.warmhouse.devicecontrol.model.dto;

import java.util.UUID;

public class CommandStatusResponse {

    private UUID commandId;
    private String status;

    public CommandStatusResponse() {}

    public CommandStatusResponse(UUID commandId, String status) {
        this.commandId = commandId;
        this.status = status;
    }

    public UUID getCommandId() { return commandId; }
    public void setCommandId(UUID commandId) { this.commandId = commandId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
