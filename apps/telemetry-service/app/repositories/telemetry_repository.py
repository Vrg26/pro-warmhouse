from typing import Any
from uuid import UUID

import asyncpg

from app.repositories.base_repository import BaseRepository


class TelemetryRepository(BaseRepository):
    """PostgreSQL implementation of the telemetry data repository."""

    def __init__(self, pool: asyncpg.Pool):
        self._pool = pool

    async def save(self, entity: dict[str, Any]) -> dict[str, Any]:
        """Save a telemetry reading to the database."""
        query = """
            INSERT INTO telemetry_readings (device_id, metric_name, value, unit)
            VALUES ($1, $2, $3, $4)
            RETURNING id, device_id, metric_name, value, unit, timestamp
        """
        async with self._pool.acquire() as conn:
            row = await conn.fetchrow(
                query,
                entity["device_id"],
                entity["metric_name"],
                entity["value"],
                entity.get("unit"),
            )
        return dict(row)

    async def find_by_id(self, entity_id: UUID) -> dict[str, Any] | None:
        """Find a telemetry reading by ID."""
        query = """
            SELECT id, device_id, metric_name, value, unit, timestamp
            FROM telemetry_readings WHERE id = $1
        """
        async with self._pool.acquire() as conn:
            row = await conn.fetchrow(query, entity_id)
        return dict(row) if row else None

    async def find_by_device_id(
        self, device_id: int, limit: int = 100
    ) -> list[dict[str, Any]]:
        """Find telemetry readings for a device, ordered by most recent."""
        query = """
            SELECT id, device_id, metric_name, value, unit, timestamp
            FROM telemetry_readings
            WHERE device_id = $1
            ORDER BY timestamp DESC
            LIMIT $2
        """
        async with self._pool.acquire() as conn:
            rows = await conn.fetch(query, device_id, limit)
        return [dict(row) for row in rows]

    async def find_latest_by_device_id(
        self, device_id: int
    ) -> dict[str, Any] | None:
        """Find the most recent telemetry reading for a device."""
        query = """
            SELECT id, device_id, metric_name, value, unit, timestamp
            FROM telemetry_readings
            WHERE device_id = $1
            ORDER BY timestamp DESC
            LIMIT 1
        """
        async with self._pool.acquire() as conn:
            row = await conn.fetchrow(query, device_id)
        return dict(row) if row else None

    async def get_aggregates(
        self, device_id: int, metric_name: str | None = None
    ) -> dict[str, Any] | None:
        """Calculate aggregate statistics for a device's telemetry."""
        if metric_name:
            query = """
                SELECT
                    device_id,
                    metric_name,
                    AVG(value) as avg_value,
                    MIN(value) as min_value,
                    MAX(value) as max_value,
                    COUNT(*) as count,
                    MIN(timestamp) as period_start,
                    MAX(timestamp) as period_end
                FROM telemetry_readings
                WHERE device_id = $1 AND metric_name = $2
                GROUP BY device_id, metric_name
            """
            async with self._pool.acquire() as conn:
                row = await conn.fetchrow(query, device_id, metric_name)
        else:
            query = """
                SELECT
                    device_id,
                    metric_name,
                    AVG(value) as avg_value,
                    MIN(value) as min_value,
                    MAX(value) as max_value,
                    COUNT(*) as count,
                    MIN(timestamp) as period_start,
                    MAX(timestamp) as period_end
                FROM telemetry_readings
                WHERE device_id = $1
                GROUP BY device_id, metric_name
            """
            async with self._pool.acquire() as conn:
                row = await conn.fetchrow(query, device_id)
        return dict(row) if row else None
