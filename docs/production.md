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
4. Mantener Stripe y la creación de nuevas campañas de Merch desactivados en producción.
5. Si la campaña de Merch ya finalizada fue una venta real, documentar quién actuó como responsable de esa operación y configurar `MERCH_SELLER`.
6. Comprar/configurar el dominio.
7. Contratar un servidor o plataforma compatible con Docker.
8. Configurar backups externos y probar una restauración.
9. Verificar el dominio de email.

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

El build de producción exige los datos legales principales y fallará si no están definidos.

Antes de construir, ejecuta:

```bash
chmod +x scripts/check-production-env.sh
./scripts/check-production-env.sh
```

No continúes con el despliegue si el preflight devuelve errores.

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

## Merch y Stripe

Para este lanzamiento no hay venta activa de Merch.

Mantén en producción:

```
STRIPE_ENABLED=false
MERCH_NEW_CAMPAIGNS_ENABLED=false
```

No configures claves LIVE ni webhook de Stripe mientras no exista una nueva campaña aprobada y revisada legalmente.

Los pedidos de la campaña finalizada pueden seguir disponibles para consulta, seguimiento e incidencias.

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
   - registro y login,
   - creación automática de socio y acceso al carnet,
   - rutas, eventos y circuitos,
   - perfil, pedidos históricos y preferencias,
   - Merch visible únicamente como campaña finalizada,
   - comprobar que no existe compra ni apertura de nueva campaña,
   - Aviso legal, Privacidad y Cookies sin textos [PENDIENTE],
   - logout y eliminación de la sesión local.
10. revisar las cabeceras HTTPS y ejecutar un backup de prueba.

## Rollback

El código puede volver al commit anterior y reconstruirse.

Las migraciones de base de datos no deben revertirse manualmente en caliente. Si una release requiere rollback de datos, utiliza el backup previo y sigue un procedimiento controlado de restauración.
