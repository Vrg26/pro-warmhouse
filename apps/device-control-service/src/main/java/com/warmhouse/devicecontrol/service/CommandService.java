package com.warmhouse.devicecontrol.service;

import com.warmhouse.devicecontrol.model.dto.CommandRequest;
import com.warmhouse.devicecontrol.model.dto.CommandResponse;
import com.warmhouse.devicecontrol.model.dto.CommandStatusResponse;

import java.util.UUID;

/**
 * Service interface for device command operations.
 */
public interface CommandService {

    /**
     * Create and execute a command for a device.
     */
    CommandResponse createCommand(int deviceId, CommandRequest request);

    /**
     * Get full command details by ID.
     */
    CommandResponse getCommand(UUID commandId);

    /**
     * Get command execution status.
     */
    CommandStatusResponse getCommandStatus(UUID commandId);
}
