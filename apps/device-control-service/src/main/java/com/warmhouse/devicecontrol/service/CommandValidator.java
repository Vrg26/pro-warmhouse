package com.warmhouse.devicecontrol.service;

import com.warmhouse.devicecontrol.exception.InvalidCommandException;
import com.warmhouse.devicecontrol.model.CommandType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Validates command parameters against known constraints per command type.
 */
@Component
public class CommandValidator {

    public void validate(CommandType type, Map<String, Object> parameters) {
        if (parameters == null) {
            parameters = Map.of();
        }

        switch (type) {
            case SET_TEMPERATURE -> validateSetTemperature(parameters);
            case TOGGLE_POWER -> validateTogglePower(parameters);
            case SET_BRIGHTNESS -> validateSetBrightness(parameters);
        }
    }

    private void validateSetTemperature(Map<String, Object> params) {
        Object target = params.get("target_temperature");
        if (target == null) {
            throw new InvalidCommandException("Parameter 'target_temperature' is required for SET_TEMPERATURE");
        }
        double temp = ((Number) target).doubleValue();
        if (temp < 10.0 || temp > 35.0) {
            throw new InvalidCommandException(
                    "target_temperature must be between 10.0 and 35.0, got: " + temp);
        }
    }

    private void validateTogglePower(Map<String, Object> params) {
        Object state = params.get("state");
        if (state == null) {
            throw new InvalidCommandException("Parameter 'state' is required for TOGGLE_POWER");
        }
        String stateStr = state.toString().toLowerCase();
        if (!stateStr.equals("on") && !stateStr.equals("off")) {
            throw new InvalidCommandException("Parameter 'state' must be 'on' or 'off', got: " + stateStr);
        }
    }

    private void validateSetBrightness(Map<String, Object> params) {
        Object level = params.get("brightness_level");
        if (level == null) {
            throw new InvalidCommandException("Parameter 'brightness_level' is required for SET_BRIGHTNESS");
        }
        double brightness = ((Number) level).doubleValue();
        if (brightness < 0 || brightness > 100) {
            throw new InvalidCommandException(
                    "brightness_level must be between 0 and 100, got: " + brightness);
        }
    }
}
