from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application settings loaded from environment variables."""

    database_url: str = "postgresql://postgres:postgres@localhost:5432/telemetry_db"
    monolith_url: str = "http://app:8080"
    port: int = 8082
    debug: bool = False

    class Config:
        env_prefix = ""
        case_sensitive = False


settings = Settings()
