package com.warmhouse.devicecontrol.model;

public enum CommandStatus {
    PENDING,
    VALIDATING,
    QUEUED,
    EXECUTING,
    SUCCESS,
    FAILED,
    TIMEOUT
}
