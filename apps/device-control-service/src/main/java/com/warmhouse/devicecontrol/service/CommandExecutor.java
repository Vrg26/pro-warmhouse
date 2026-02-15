package com.warmhouse.devicecontrol.service;

import com.warmhouse.devicecontrol.model.Command;
import com.warmhouse.devicecontrol.model.CommandResult;
import com.warmhouse.devicecontrol.model.CommandStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Simulates command execution for MVP.
 * In the target architecture, this would send commands via MQTT to the Protocol Gateway.
 */
@Component
public class CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
    private final Random random = new Random();

    /**
     * Simulate command execution with random delay and 80% success rate.
     */
    public CommandResult execute(Command command) {
        long startTime = System.currentTimeMillis();

        // Simulate execution delay (100-500ms)
        try {
            Thread.sleep(100 + random.nextInt(400));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long executionTime = System.currentTimeMillis() - startTime;

        // 80% success rate
        boolean success = random.nextDouble() < 0.8;

        if (success) {
            logger.info("Command {} executed successfully for device {}",
                    command.getId(), command.getDeviceId());
            return new CommandResult(
                    command.getId(),
                    CommandStatus.SUCCESS,
                    "{\"message\": \"Command executed successfully\"}",
                    null,
                    (int) executionTime
            );
        } else {
            logger.warn("Command {} failed for device {}",
                    command.getId(), command.getDeviceId());
            return new CommandResult(
                    command.getId(),
                    CommandStatus.FAILED,
                    null,
                    "Device did not respond within timeout",
                    (int) executionTime
            );
        }
    }
}
