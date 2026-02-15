package com.warmhouse.devicecontrol.controller;

import com.warmhouse.devicecontrol.model.dto.CommandRequest;
import com.warmhouse.devicecontrol.model.dto.CommandResponse;
import com.warmhouse.devicecontrol.model.dto.CommandStatusResponse;
import com.warmhouse.devicecontrol.service.CommandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class CommandController {

    private final CommandService commandService;

    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping("/devices/{deviceId}/commands")
    public ResponseEntity<CommandResponse> createCommand(
            @PathVariable int deviceId,
            @Valid @RequestBody CommandRequest request) {
        CommandResponse response = commandService.createCommand(deviceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/commands/{commandId}")
    public ResponseEntity<CommandResponse> getCommand(@PathVariable UUID commandId) {
        CommandResponse response = commandService.getCommand(commandId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/commands/{commandId}/status")
    public ResponseEntity<CommandStatusResponse> getCommandStatus(@PathVariable UUID commandId) {
        CommandStatusResponse response = commandService.getCommandStatus(commandId);
        return ResponseEntity.ok(response);
    }
}
