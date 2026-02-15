import asyncpg
import logging

logger = logging.getLogger(__name__)

# Global connection pool
_pool: asyncpg.Pool | None = None

SCHEMA_SQL = """
CREATE TABLE IF NOT EXISTS telemetry_readings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id INTEGER NOT NULL,
    metric_name VARCHAR(50) NOT NULL,
    value FLOAT NOT NULL,
    unit VARCHAR(20),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_telemetry_device_id ON telemetry_readings(device_id);
CREATE INDEX IF NOT EXISTS idx_telemetry_timestamp ON telemetry_readings(timestamp);
CREATE INDEX IF NOT EXISTS idx_telemetry_device_metric ON telemetry_readings(device_id, metric_name);

CREATE TABLE IF NOT EXISTS telemetry_aggregates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id INTEGER NOT NULL,
    metric_name VARCHAR(50) NOT NULL,
    avg_value FLOAT NOT NULL,
    min_value FLOAT NOT NULL,
    max_value FLOAT NOT NULL,
    count INTEGER NOT NULL,
    period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_telemetry_agg_device ON telemetry_aggregates(device_id, metric_name);
"""


async def init_db(database_url: str) -> asyncpg.Pool:
    """Initialize connection pool and create schema."""
    global _pool
    _pool = await asyncpg.create_pool(database_url, min_size=2, max_size=10)
    async with _pool.acquire() as conn:
        await conn.execute(SCHEMA_SQL)
    logger.info("Database initialized successfully")
    return _pool


async def close_db():
    """Close connection pool."""
    global _pool
    if _pool:
        await _pool.close()
        _pool = None
        logger.info("Database connection pool closed")


def get_pool() -> asyncpg.Pool:
    """Get the current connection pool."""
    if _pool is None:
        raise RuntimeError("Database pool is not initialized")
    return _pool
