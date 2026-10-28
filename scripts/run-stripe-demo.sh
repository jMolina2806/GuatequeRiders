#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
EVENTS="checkout.session.completed,checkout.session.async_payment_succeeded,checkout.session.async_payment_failed,checkout.session.expired"
WEBHOOK_URL="http://localhost:8080/api/payments/stripe/webhook"
DEFAULT_ENV="$ROOT_DIR/.env"
STRIPE_ENV="$ROOT_DIR/.env.stripe.local"

if ! command -v stripe >/dev/null 2>&1; then
  echo "Stripe CLI no está instalado."
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker no está disponible."
  exit 1
fi

if [[ -f "$DEFAULT_ENV" ]]; then
  set -a
  source "$DEFAULT_ENV"
  set +a
fi

if [[ -f "$STRIPE_ENV" ]]; then
  set -a
  source "$STRIPE_ENV"
  set +a
fi

if [[ -z "${STRIPE_SECRET_KEY:-}" ]]; then
  echo "Falta STRIPE_SECRET_KEY."
  echo "Añádela a $DEFAULT_ENV o $STRIPE_ENV:"
  echo "STRIPE_SECRET_KEY=sk_test_..."
  exit 1
fi

if [[ "$STRIPE_SECRET_KEY" != sk_test_* ]]; then
  echo "La demo solo admite una clave Stripe TEST (sk_test_...)."
  exit 1
fi

if ss -ltn 2>/dev/null | grep -Eq '[:.]8080[[:space:]]'; then
  echo "El puerto 8080 ya está ocupado. Cierra el backend anterior antes de iniciar la demo."
  exit 1
fi

if ss -ltn 2>/dev/null | grep -Eq '[:.]5173[[:space:]]'; then
  echo "El puerto 5173 ya está ocupado. Cierra el frontend anterior antes de iniciar la demo."
  exit 1
fi

echo "Iniciando PostgreSQL..."
cd "$ROOT_DIR"
docker compose up -d postgres

until docker compose exec -T postgres pg_isready -U postgres -d guatequeriders >/dev/null 2>&1; do
  sleep 1
done

echo "Obteniendo secreto local del webhook de Stripe..."
STRIPE_WEBHOOK_SECRET="$(stripe --api-key "$STRIPE_SECRET_KEY" listen --events "$EVENTS" --forward-to "$WEBHOOK_URL" --print-secret)"
export STRIPE_WEBHOOK_SECRET
export SPRING_PROFILES_ACTIVE=dev
export STRIPE_ENABLED=true
export STRIPE_MODE=test
export APP_PUBLIC_URL="${APP_PUBLIC_URL:-http://localhost:5173}"

cleanup() {
  echo
  echo "Deteniendo demo..."
  for pid in "${FRONTEND_PID:-}" "${BACKEND_PID:-}" "${STRIPE_PID:-}"; do
    if [[ -n "$pid" ]]; then
      kill "$pid" 2>/dev/null || true
    fi
  done
  wait 2>/dev/null || true
}

trap cleanup EXIT INT TERM

echo "Iniciando Stripe listener..."
stripe --api-key "$STRIPE_SECRET_KEY" listen \
  --events "$EVENTS" \
  --forward-to "$WEBHOOK_URL" &
STRIPE_PID=$!

echo "Iniciando backend..."
(
  cd "$ROOT_DIR/apps/api"
  ./mvnw spring-boot:run
) &
BACKEND_PID=$!

echo "Iniciando frontend..."
(
  cd "$ROOT_DIR/apps/web"
  npm run dev
) &
FRONTEND_PID=$!

echo
echo "Demo iniciada:"
echo "  Frontend: $APP_PUBLIC_URL"
echo "  Backend:  http://localhost:8080"
echo "  Stripe:   TEST + webhooks locales"
echo
echo "Cuando termines, pulsa Ctrl+C para cerrar frontend, backend y Stripe listener."

wait
