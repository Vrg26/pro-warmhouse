package com.warmhouse.devicecontrol.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "command_results")
public class CommandResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "command_id", nullable = false)
    private UUID commandId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommandStatus status;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "executed_at", nullable = false)
    private OffsetDateTime executedAt = OffsetDateTime.now();

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    public CommandResult() {}

    public CommandResult(UUID commandId, CommandStatus status, String response,
                         String errorMessage, Integer executionTimeMs) {
        this.commandId = commandId;
        this.status = status;
        this.response = response;
        this.errorMessage = errorMessage;
        this.executedAt = OffsetDateTime.now();
        this.executionTimeMs = executionTimeMs;
    }

    // Getters and setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCommandId() { return commandId; }
    public void setCommandId(UUID commandId) { this.commandId = commandId; }

    public CommandStatus getStatus() { return status; }
    public void setStatus(CommandStatus status) { this.status = status; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public OffsetDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(OffsetDateTime executedAt) { this.executedAt = executedAt; }

    public Integer getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Integer executionTimeMs) { this.executionTimeMs = executionTimeMs; }
}
