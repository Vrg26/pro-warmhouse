package com.warmhouse.devicecontrol.model.dto;

import com.warmhouse.devicecontrol.model.Command;
import com.warmhouse.devicecontrol.model.CommandResult;
import java.time.OffsetDateTime;
import java.util.UUID;

public class CommandResponse {

    private UUID id;
    private Integer deviceId;
    private String type;
    private String parameters;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Integer retryCount;
    private CommandResultDto result;

    public static CommandResponse fromEntity(Command command, CommandResult commandResult) {
        CommandResponse response = new CommandResponse();
        response.setId(command.getId());
        response.setDeviceId(command.getDeviceId());
        response.setType(command.getType().name());
        response.setParameters(command.getParameters());
        response.setStatus(command.getStatus().name());
        response.setCreatedAt(command.getCreatedAt());
        response.setUpdatedAt(command.getUpdatedAt());
        response.setRetryCount(command.getRetryCount());

        if (commandResult != null) {
            CommandResultDto resultDto = new CommandResultDto();
            resultDto.setId(commandResult.getId());
            resultDto.setStatus(commandResult.getStatus().name());
            resultDto.setResponse(commandResult.getResponse());
            resultDto.setErrorMessage(commandResult.getErrorMessage());
            resultDto.setExecutedAt(commandResult.getExecutedAt());
            resultDto.setExecutionTimeMs(commandResult.getExecutionTimeMs());
            response.setResult(resultDto);
        }
        return response;
    }

    // Getters and setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Integer getDeviceId() { return deviceId; }
    public void setDeviceId(Integer deviceId) { this.deviceId = deviceId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public CommandResultDto getResult() { return result; }
    public void setResult(CommandResultDto result) { this.result = result; }

    public static class CommandResultDto {
        private UUID id;
        private String status;
        private String response;
        private String errorMessage;
        private OffsetDateTime executedAt;
        private Integer executionTimeMs;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getResponse() { return response; }
        public void setResponse(String response) { this.response = response; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public OffsetDateTime getExecutedAt() { return executedAt; }
        public void setExecutedAt(OffsetDateTime executedAt) { this.executedAt = executedAt; }

        public Integer getExecutionTimeMs() { return executionTimeMs; }
        public void setExecutionTimeMs(Integer executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    }
}
