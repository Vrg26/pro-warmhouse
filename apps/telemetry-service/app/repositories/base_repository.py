from abc import ABC, abstractmethod
from typing import Any
from uuid import UUID


class BaseRepository(ABC):
    """Abstract base repository defining the data access interface."""

    @abstractmethod
    async def save(self, entity: dict[str, Any]) -> dict[str, Any]:
        """Save an entity and return it with generated fields."""
        raise NotImplementedError

    @abstractmethod
    async def find_by_id(self, entity_id: UUID) -> dict[str, Any] | None:
        """Find an entity by its unique identifier."""
        raise NotImplementedError

    @abstractmethod
    async def find_by_device_id(
        self, device_id: int, limit: int = 100
    ) -> list[dict[str, Any]]:
        """Find all entities associated with a device."""
        raise NotImplementedError
