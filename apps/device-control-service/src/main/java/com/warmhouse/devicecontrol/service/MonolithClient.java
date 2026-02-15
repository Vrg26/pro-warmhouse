package com.warmhouse.devicecontrol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP client for communicating with the monolith application.
 * Used to update sensor values after successful command execution.
 */
@Component
public class MonolithClient {

    private static final Logger logger = LoggerFactory.getLogger(MonolithClient.class);

    private final RestTemplate restTemplate;
    private final String monolithUrl;

    public MonolithClient(RestTemplate restTemplate,
                          @Value("${app.monolith-url}") String monolithUrl) {
        this.restTemplate = restTemplate;
        this.monolithUrl = monolithUrl;
    }

    /**
     * Update a sensor's value in the monolith after a successful command.
     */
    public void updateSensorValue(int deviceId, double value, String status) {
        try {
            String url = monolithUrl + "/api/v1/sensors/" + deviceId + "/value";
            Map<String, Object> body = Map.of(
                    "value", value,
                    "status", status
            );
            restTemplate.patchForObject(url, body, String.class);
            logger.info("Updated sensor {} in monolith: value={}, status={}",
                    deviceId, value, status);
        } catch (Exception e) {
            logger.warn("Failed to update sensor {} in monolith: {}",
                    deviceId, e.getMessage());
        }
    }
}
