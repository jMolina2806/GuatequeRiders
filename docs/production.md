# Guateque Riders · Producción

## Estado

La aplicación está preparada para desplegarse con:

- Frontend React compilado y servido por Caddy.
- HTTPS automático.
- API Spring Boot 4 / Java 17.
- PostgreSQL 16 persistente.
- Volumen persistente para uploads.
- Flyway para migraciones.
- Stripe configurable por variables de entorno.
- SMTP configurable por variables de entorno.
- Backups manuales de base de datos y uploads.

## Bloqueos antes de publicar

No publicar hasta completar estos puntos:

1. Definir quién actúa como responsable de la plataforma y del tratamiento de datos antes de abrir el registro público.
2. Configurar `LEGAL_RESPONSIBLE`, `LEGAL_CONTACT_EMAIL` y `LEGAL_CONTACT_ADDRESS` con datos reales antes de compilar el frontend de producción.
3. Crear un email de contacto de la comunidad cuando exista dominio.
4. Si se va a activar venta real de Merch, definir antes quién recibe los pagos y actúa como vendedor o responsable de la operación y configurar `MERCH_SELLER`.
5. Revisar las condiciones reales de entrega, costes, cancelaciones y devoluciones antes de activar Stripe LIVE.
6. Comprar/configurar el dominio.
7. Contratar un servidor o plataforma compatible con Docker.
8. Configurar backups externos y probar una restauración.
9. Verificar el dominio de email.
10. Activar Stripe LIVE únicamente después de probar el despliegue completo y cerrar los puntos anteriores.

## Dominio y HTTPS

Configura el DNS del dominio para que apunte a la IP pública del servidor.

El servidor debe aceptar tráfico entrante en:

- TCP 80
- TCP 443
- UDP 443 opcional para HTTP/3

Caddy obtiene y renueva automáticamente el certificado HTTPS cuando `APP_DOMAIN` contiene un dominio público válido.

## Variables de producción

Crea el archivo local:

```bash
cp .env.production.example .env.production
```

No subas `.env.production` a Git.

Genera secretos diferentes:

```bash
openssl rand -hex 64
openssl rand -hex 64
openssl rand -base64 48
```

Utiliza valores distintos para:

- `JWT_SECRET`
- `UNSUBSCRIBE_SECRET`
- `POSTGRES_PASSWORD`

Completa también los datos legales que se insertan en el frontend durante el build:

- `LEGAL_RESPONSIBLE`
- `LEGAL_CONTACT_EMAIL`
- `LEGAL_CONTACT_ADDRESS`
- `MERCH_SELLER` si se activa venta real

Si alguno queda vacío, la web mostrará un aviso de dato pendiente en las páginas legales.

## Primer despliegue

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml build
docker compose --env-file .env.production -f docker-compose.prod.yml up -d
```

Comprueba:

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml ps
curl https://TU_DOMINIO/api/health
```

## Email real

Mientras `MAIL_ENABLED=false`, la aplicación no envía emails reales.

Cuando exista dominio:

1. Añade el dominio al proveedor SMTP.
2. Publica los registros DNS que solicite el proveedor.
3. Verifica SPF y DKIM y configura DMARC cuando corresponda.
4. Configura `MAIL_*` en `.env.production`.
5. Cambia `MAIL_ENABLED=true`.
6. Reinicia únicamente la API.

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml up -d api
```

## Stripe LIVE

No reutilices claves TEST.

Antes de activar:

1. Configura la cuenta Stripe para producción.
2. Crea un webhook LIVE apuntando a:
   `https://TU_DOMINIO/api/payments/stripe/webhook`
3. Guarda la clave secreta LIVE en `STRIPE_SECRET_KEY`.
4. Guarda el secreto del webhook LIVE en `STRIPE_WEBHOOK_SECRET`.
5. Establece:
   ```
   STRIPE_ENABLED=true
   STRIPE_MODE=live
   ```
6. Reinicia la API.
7. Realiza una compra real de importe controlado y verifica pedido, webhook, email y estado.

## Backups

Ejecuta:

```bash
./scripts/backup-production.sh
```

Se generan:

- dump comprimido de PostgreSQL,
- copia comprimida del volumen de uploads.

Los backups deben copiarse posteriormente a una ubicación externa al propio servidor.

Para restaurar la base:

```bash
./scripts/restore-production-db.sh backups/postgres-YYYYMMDDTHHMMSSZ.sql.gz
```

El script exige confirmación explícita.

## Release

Flujo recomendado:

1. feature → develop
2. CI verde
3. PR develop → main
4. CI de release verde
5. merge a main
6. backup previo si ya existe producción
7. pull/build/deploy
8. health check
9. smoke test:
   - registro/login,
   - rutas/eventos/circuitos,
   - perfil y notificaciones,
   - Merch,
   - pago,
   - webhook,
   - email,
   - estados del pedido.

## Rollback

El código puede volver al commit anterior y reconstruirse.

Las migraciones de base de datos no deben revertirse manualmente en caliente. Si una release requiere rollback de datos, utiliza el backup previo y sigue un procedimiento controlado de restauración.
