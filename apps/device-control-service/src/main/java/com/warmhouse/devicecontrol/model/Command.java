package com.warmhouse.devicecontrol.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "commands")
public class Command {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private Integer deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CommandType type;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommandStatus status = CommandStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    public Command() {}

    public Command(Integer deviceId, CommandType type, String parameters) {
        this.deviceId = deviceId;
        this.type = type;
        this.parameters = parameters;
        this.status = CommandStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        this.retryCount = 0;
    }

    // Getters and setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Integer getDeviceId() { return deviceId; }
    public void setDeviceId(Integer deviceId) { this.deviceId = deviceId; }

    public CommandType getType() { return type; }
    public void setType(CommandType type) { this.type = type; }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }

    public CommandStatus getStatus() { return status; }
    public void setStatus(CommandStatus status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
}
