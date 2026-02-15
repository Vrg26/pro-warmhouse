#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE telemetry_db;
    CREATE DATABASE device_control_db;
EOSQL
