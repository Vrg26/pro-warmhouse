from fastapi import APIRouter, Depends, HTTPException, Query

from app.models.telemetry import TelemetryCreate, TelemetryReading, TelemetryAggregate
from app.services.telemetry_service import TelemetryService

router = APIRouter(prefix="/api/v1", tags=["telemetry"])


def get_telemetry_service() -> TelemetryService:
    """Dependency injection — overridden in main.py at startup."""
    raise RuntimeError("TelemetryService dependency not configured")


@router.post("/telemetry", response_model=TelemetryReading, status_code=201)
async def ingest_telemetry(
    data: TelemetryCreate,
    service: TelemetryService = Depends(get_telemetry_service),
):
    """Ingest a new telemetry reading."""
    try:
        reading = await service.ingest_reading(data)
        return reading
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.get(
    "/devices/{device_id}/telemetry",
    response_model=list[TelemetryReading],
)
async def get_device_telemetry(
    device_id: int,
    limit: int = Query(default=100, ge=1, le=1000),
    service: TelemetryService = Depends(get_telemetry_service),
):
    """Get telemetry history for a device."""
    readings = await service.get_device_telemetry(device_id, limit)
    return readings


@router.get(
    "/devices/{device_id}/telemetry/current",
    response_model=TelemetryReading | None,
)
async def get_current_telemetry(
    device_id: int,
    service: TelemetryService = Depends(get_telemetry_service),
):
    """Get the latest telemetry reading for a device."""
    reading = await service.get_current_reading(device_id)
    if reading is None:
        raise HTTPException(
            status_code=404,
            detail=f"No telemetry data found for device {device_id}",
        )
    return reading


@router.get(
    "/devices/{device_id}/telemetry/aggregate",
    response_model=TelemetryAggregate | None,
)
async def get_telemetry_aggregates(
    device_id: int,
    metric_name: str | None = Query(default=None),
    service: TelemetryService = Depends(get_telemetry_service),
):
    """Get aggregated telemetry statistics for a device."""
    aggregate = await service.get_aggregates(device_id, metric_name)
    if aggregate is None:
        raise HTTPException(
            status_code=404,
            detail=f"No telemetry aggregates found for device {device_id}",
        )
    return aggregate
