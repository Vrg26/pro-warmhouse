class DataValidator:
    """Validates telemetry data against known metric constraints."""

    # Valid ranges for known metric types
    METRIC_RANGES: dict[str, tuple[float, float]] = {
        "temperature": (-50.0, 100.0),
        "humidity": (0.0, 100.0),
        "pressure": (800.0, 1200.0),
        "brightness": (0.0, 100.0),
    }

    VALID_UNITS: dict[str, list[str]] = {
        "temperature": ["°C", "C", "°F", "F"],
        "humidity": ["%"],
        "pressure": ["hPa", "mbar"],
        "brightness": ["%", "lux"],
    }

    def validate(self, metric_name: str, value: float, unit: str | None = None) -> tuple[bool, str | None]:
        """
        Validate a telemetry reading.

        Returns:
            Tuple of (is_valid, error_message).
        """
        if not metric_name or not metric_name.strip():
            return False, "Metric name cannot be empty"

        metric_lower = metric_name.lower()

        # Check value range for known metrics
        if metric_lower in self.METRIC_RANGES:
            min_val, max_val = self.METRIC_RANGES[metric_lower]
            if value < min_val or value > max_val:
                return False, (
                    f"Value {value} out of range [{min_val}, {max_val}] "
                    f"for metric '{metric_name}'"
                )

        # Check unit validity for known metrics
        if unit and metric_lower in self.VALID_UNITS:
            if unit not in self.VALID_UNITS[metric_lower]:
                return False, (
                    f"Invalid unit '{unit}' for metric '{metric_name}'. "
                    f"Expected one of: {self.VALID_UNITS[metric_lower]}"
                )

        return True, None
