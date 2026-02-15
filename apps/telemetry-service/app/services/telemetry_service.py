import logging

from app.models.telemetry import (
    TelemetryAggregate,
    TelemetryCreate,
    TelemetryReading,
)
from app.repositories.telemetry_repository import TelemetryRepository
from app.services.data_validator import DataValidator

logger = logging.getLogger(__name__)


class TelemetryService:
    """Business logic layer for telemetry operations."""

    def __init__(self, repository: TelemetryRepository, validator: DataValidator):
        self._repository = repository
        self._validator = validator

    async def ingest_reading(self, data: TelemetryCreate) -> TelemetryReading:
        """Validate and save a new telemetry reading."""
        is_valid, error = self._validator.validate(
            data.metric_name, data.value, data.unit
        )
        if not is_valid:
            raise ValueError(f"Validation failed: {error}")

        row = await self._repository.save(data.model_dump())
        logger.info(
            "Telemetry reading saved: device_id=%d, metric=%s, value=%.2f",
            data.device_id,
            data.metric_name,
            data.value,
        )
        return TelemetryReading(**row)

    async def get_device_telemetry(
        self, device_id: int, limit: int = 100
    ) -> list[TelemetryReading]:
        """Get telemetry history for a device."""
        rows = await self._repository.find_by_device_id(device_id, limit)
        return [TelemetryReading(**row) for row in rows]

    async def get_current_reading(
        self, device_id: int
    ) -> TelemetryReading | None:
        """Get the latest telemetry reading for a device."""
        row = await self._repository.find_latest_by_device_id(device_id)
        return TelemetryReading(**row) if row else None

    async def get_aggregates(
        self, device_id: int, metric_name: str | None = None
    ) -> TelemetryAggregate | None:
        """Get aggregated statistics for a device's telemetry."""
        row = await self._repository.get_aggregates(device_id, metric_name)
        return TelemetryAggregate(**row) if row else None
