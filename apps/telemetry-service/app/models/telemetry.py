from datetime import datetime
from uuid import UUID
from pydantic import BaseModel, Field


class TelemetryBase(BaseModel):
    """Base telemetry model with shared fields."""

    device_id: int
    metric_name: str
    value: float
    unit: str | None = None


class TelemetryCreate(TelemetryBase):
    """Model for creating a telemetry reading."""
    pass


class TelemetryReading(TelemetryBase):
    """Full telemetry reading with generated fields."""

    id: UUID
    timestamp: datetime

    class Config:
        from_attributes = True


class TelemetryAggregate(BaseModel):
    """Aggregated telemetry statistics."""

    device_id: int
    metric_name: str
    avg_value: float
    min_value: float
    max_value: float
    count: int
    period_start: datetime
    period_end: datetime

    class Config:
        from_attributes = True


class TelemetryResponse(BaseModel):
    """Generic API response wrapper."""

    status: str = "ok"
    data: list[TelemetryReading] | TelemetryReading | TelemetryAggregate | None = None
    message: str | None = None
