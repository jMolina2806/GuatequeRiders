#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Uso: $0 <backup.sql.gz> [archivo .env.production]" >&2
  exit 1
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKUP_FILE="$(realpath "$1")"
ENV_FILE="${2:-$ROOT_DIR/.env.production}"

if [[ ! -f "$BACKUP_FILE" ]]; then
  echo "No existe el backup: $BACKUP_FILE" >&2
  exit 1
fi

if [[ ! -f "$ENV_FILE" ]]; then
  echo "No existe el archivo de entorno: $ENV_FILE" >&2
  exit 1
fi

echo "ATENCIÓN: esta operación reemplazará los datos de la base de datos de producción."
read -r -p "Escribe RESTAURAR para continuar: " confirmation

if [[ "$confirmation" != "RESTAURAR" ]]; then
  echo "Restauración cancelada."
  exit 1
fi

gunzip -c "$BACKUP_FILE"   | docker compose --env-file "$ENV_FILE" -f "$ROOT_DIR/docker-compose.prod.yml"       exec -T postgres sh -c 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" "$POSTGRES_DB"'

echo "Restauración completada."
