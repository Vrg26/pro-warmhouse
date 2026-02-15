package com.warmhouse.devicecontrol.model.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class CommandRequest {

    @NotNull(message = "Command type is required")
    private String type;

    private Map<String, Object> parameters;

    public CommandRequest() {}

    public CommandRequest(String type, Map<String, Object> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}
