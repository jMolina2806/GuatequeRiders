#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${1:-$ROOT_DIR/.env.production}"
BACKUP_DIR="${BACKUP_DIR:-$ROOT_DIR/backups}"
STAMP="$(date -u +%Y%m%dT%H%M%SZ)"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "No existe el archivo de entorno: $ENV_FILE" >&2
  exit 1
fi

mkdir -p "$BACKUP_DIR"

echo "Creando backup de PostgreSQL..."
docker compose --env-file "$ENV_FILE" -f "$ROOT_DIR/docker-compose.prod.yml"   exec -T postgres sh -c 'pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB"'   | gzip > "$BACKUP_DIR/postgres-$STAMP.sql.gz"

echo "Creando backup de uploads..."
docker run --rm   -v guatequeriders-uploads-prod:/data:ro   -v "$BACKUP_DIR":/backup   alpine:3.22   sh -c "tar -czf /backup/uploads-$STAMP.tar.gz -C /data ."

echo "Backup completado:"
echo "  $BACKUP_DIR/postgres-$STAMP.sql.gz"
echo "  $BACKUP_DIR/uploads-$STAMP.tar.gz"
