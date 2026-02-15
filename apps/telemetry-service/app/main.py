import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.config import settings
from app.controllers.telemetry_controller import get_telemetry_service, router
from app.database import close_db, get_pool, init_db
from app.repositories.telemetry_repository import TelemetryRepository
from app.services.data_validator import DataValidator
from app.services.telemetry_service import TelemetryService

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Global service instance set during startup
_telemetry_service: TelemetryService | None = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan: initialize and cleanup resources."""
    global _telemetry_service

    logger.info("Starting Telemetry Service on port %d", settings.port)

    # Initialize database
    pool = await init_db(settings.database_url)

    # Wire up dependencies
    repository = TelemetryRepository(pool)
    validator = DataValidator()
    _telemetry_service = TelemetryService(repository, validator)

    logger.info("Telemetry Service initialized successfully")
    yield

    # Cleanup
    await close_db()
    logger.info("Telemetry Service shut down")


app = FastAPI(
    title="Telemetry Service",
    description="Microservice for collecting and querying IoT telemetry data",
    version="1.0.0",
    lifespan=lifespan,
)


def _get_telemetry_service() -> TelemetryService:
    """Provide the TelemetryService instance for dependency injection."""
    if _telemetry_service is None:
        raise RuntimeError("TelemetryService is not initialized")
    return _telemetry_service


# Override the dependency placeholder in the controller
app.dependency_overrides[get_telemetry_service] = _get_telemetry_service

# Include routes
app.include_router(router)


@app.get("/health")
async def health():
    """Health check endpoint."""
    return {"status": "ok", "service": "telemetry"}
