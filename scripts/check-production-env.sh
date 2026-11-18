#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${1:-$ROOT_DIR/.env.production}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "ERROR: no existe $ENV_FILE" >&2
  exit 1
fi

set -a
source "$ENV_FILE"
set +a

errors=0

fail() {
  echo "ERROR: $1" >&2
  errors=$((errors + 1))
}

require_value() {
  local name="$1"
  local value="${!name:-}"
  if [[ -z "$value" ]]; then
    fail "$name está vacío"
  fi
}

require_not_placeholder() {
  local name="$1"
  local value="${!name:-}"
  if [[ "$value" == *REEMPLAZAR* || "$value" == *PENDIENTE* || "$value" == *example* || "$value" == *EJEMPLO* || "$value" == *"NOMBRE COMPLETO"* || "$value" == *"DIRECCION DE CONTACTO"* ]]; then
    fail "$name contiene un valor de ejemplo o pendiente"
  fi
}

require_min_length() {
  local name="$1"
  local min="$2"
  local value="${!name:-}"
  if (( ${#value} < min )); then
    fail "$name debe tener al menos $min caracteres"
  fi
}

for name in APP_DOMAIN POSTGRES_PASSWORD JWT_SECRET UNSUBSCRIBE_SECRET LEGAL_RESPONSIBLE LEGAL_CONTACT_EMAIL LEGAL_CONTACT_ADDRESS; do
  require_value "$name"
  require_not_placeholder "$name"
done

require_min_length POSTGRES_PASSWORD 24
require_min_length JWT_SECRET 64
require_min_length UNSUBSCRIBE_SECRET 48

if [[ "${APP_DOMAIN:-}" == "localhost" || "${APP_DOMAIN:-}" == 127.* || "${APP_DOMAIN:-}" == http*://* ]]; then
  fail "APP_DOMAIN debe ser solo un dominio público, sin protocolo"
fi

if [[ "${LEGAL_CONTACT_EMAIL:-}" != *@*.* ]]; then
  fail "LEGAL_CONTACT_EMAIL no parece un email válido"
fi

if [[ "${STRIPE_ENABLED:-false}" != "false" && "${STRIPE_ENABLED:-false}" != "true" ]]; then
  fail "STRIPE_ENABLED debe ser true o false"
fi

if [[ "${STRIPE_ENABLED:-false}" == "true" ]]; then
  if [[ "${STRIPE_MODE:-test}" != "live" ]]; then
    fail "STRIPE_MODE debe ser live para cobrar en producción"
  fi
  for name in STRIPE_SECRET_KEY STRIPE_WEBHOOK_SECRET MERCH_SELLER; do
    require_value "$name"
    require_not_placeholder "$name"
  done
  if [[ "${STRIPE_SECRET_KEY:-}" != sk_live_* ]]; then
    fail "STRIPE_SECRET_KEY debe ser una clave live de Stripe"
  fi
  if [[ "${STRIPE_WEBHOOK_SECRET:-}" != whsec_* ]]; then
    fail "STRIPE_WEBHOOK_SECRET debe ser un secreto webhook de Stripe"
  fi
fi

if [[ "${MAIL_ENABLED:-false}" == "true" ]]; then
  for name in MAIL_HOST MAIL_USERNAME MAIL_PASSWORD MAIL_FROM; do
    require_value "$name"
    require_not_placeholder "$name"
  done
fi

if (( errors > 0 )); then
  echo
  echo "Preflight fallido: $errors problema(s)." >&2
  exit 1
fi

echo "Preflight de producción correcto."
echo "Dominio: $APP_DOMAIN"
echo "Responsable legal configurado."
echo "Creación de campañas preparada: ${MERCH_NEW_CAMPAIGNS_ENABLED:-true}."
echo "Stripe activo: ${STRIPE_ENABLED:-false}."
