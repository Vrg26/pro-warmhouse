package com.warmhouse.devicecontrol.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warmhouse.devicecontrol.exception.DeviceNotFoundException;
import com.warmhouse.devicecontrol.exception.InvalidCommandException;
import com.warmhouse.devicecontrol.model.Command;
import com.warmhouse.devicecontrol.model.CommandResult;
import com.warmhouse.devicecontrol.model.CommandStatus;
import com.warmhouse.devicecontrol.model.CommandType;
import com.warmhouse.devicecontrol.model.dto.CommandRequest;
import com.warmhouse.devicecontrol.model.dto.CommandResponse;
import com.warmhouse.devicecontrol.model.dto.CommandStatusResponse;
import com.warmhouse.devicecontrol.repository.CommandRepository;
import com.warmhouse.devicecontrol.repository.CommandResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class CommandServiceImpl implements CommandService {

    private static final Logger logger = LoggerFactory.getLogger(CommandServiceImpl.class);

    private final CommandRepository commandRepository;
    private final CommandResultRepository commandResultRepository;
    private final CommandValidator commandValidator;
    private final CommandExecutor commandExecutor;
    private final MonolithClient monolithClient;
    private final ObjectMapper objectMapper;

    public CommandServiceImpl(CommandRepository commandRepository,
                              CommandResultRepository commandResultRepository,
                              CommandValidator commandValidator,
                              CommandExecutor commandExecutor,
                              MonolithClient monolithClient) {
        this.commandRepository = commandRepository;
        this.commandResultRepository = commandResultRepository;
        this.commandValidator = commandValidator;
        this.commandExecutor = commandExecutor;
        this.monolithClient = monolithClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public CommandResponse createCommand(int deviceId, CommandRequest request) {
        // Parse command type
        CommandType commandType;
        try {
            commandType = CommandType.valueOf(request.getType());
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandException("Unknown command type: " + request.getType());
        }

        // Validate parameters
        commandValidator.validate(commandType, request.getParameters());

        // Serialize parameters to JSON string
        String parametersJson;
        try {
            parametersJson = objectMapper.writeValueAsString(
                    request.getParameters() != null ? request.getParameters() : Map.of()
            );
        } catch (JsonProcessingException e) {
            throw new InvalidCommandException("Invalid parameters format");
        }

        // Create and save command
        Command command = new Command(deviceId, commandType, parametersJson);
        command.setStatus(CommandStatus.VALIDATING);
        command = commandRepository.save(command);

        logger.info("Command {} created for device {}: type={}", command.getId(), deviceId, commandType);

        // Transition through statuses
        command.setStatus(CommandStatus.QUEUED);
        commandRepository.save(command);

        command.setStatus(CommandStatus.EXECUTING);
        commandRepository.save(command);

        // Execute command (simulated)
        CommandResult result = commandExecutor.execute(command);
        commandResultRepository.save(result);

        // Update command with final status
        command.setStatus(result.getStatus());
        commandRepository.save(command);

        // If command succeeded, update the monolith sensor value
        if (result.getStatus() == CommandStatus.SUCCESS) {
            applyCommandToMonolith(deviceId, commandType, request.getParameters());
        }

        return CommandResponse.fromEntity(command, result);
    }

    @Override
    public CommandResponse getCommand(UUID commandId) {
        Command command = commandRepository.findById(commandId)
                .orElseThrow(() -> new DeviceNotFoundException("Command not found: " + commandId));
        CommandResult result = commandResultRepository.findByCommandId(commandId).orElse(null);
        return CommandResponse.fromEntity(command, result);
    }

    @Override
    public CommandStatusResponse getCommandStatus(UUID commandId) {
        Command command = commandRepository.findById(commandId)
                .orElseThrow(() -> new DeviceNotFoundException("Command not found: " + commandId));
        return new CommandStatusResponse(command.getId(), command.getStatus().name());
    }

    /**
     * Apply the successful command result to the monolith sensor.
     */
    private void applyCommandToMonolith(int deviceId, CommandType type, Map<String, Object> params) {
        try {
            switch (type) {
                case SET_TEMPERATURE -> {
                    double temp = ((Number) params.get("target_temperature")).doubleValue();
                    monolithClient.updateSensorValue(deviceId, temp, "active");
                }
                case TOGGLE_POWER -> {
                    String state = params.get("state").toString();
                    monolithClient.updateSensorValue(deviceId, 0, state.equals("on") ? "active" : "inactive");
                }
                case SET_BRIGHTNESS -> {
                    double level = ((Number) params.get("brightness_level")).doubleValue();
                    monolithClient.updateSensorValue(deviceId, level, "active");
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to apply command to monolith for device {}: {}", deviceId, e.getMessage());
        }
    }
}
