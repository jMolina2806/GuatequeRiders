# Seguridad de Guateque Riders

## Objetivo

Esta fase de hardening toma OWASP ASVS 5.0 como referencia práctica para una aplicación web comunitaria con autenticación, datos personales, uploads y pagos.

No implica una certificación ASVS ni sustituye una prueba de penetración externa.

## Arquitectura de confianza

La arquitectura de producción es:

```text
Internet
  |
  v
Caddy / HTTPS
  |
  v
Spring Boot
  |
  v
PostgreSQL
```

PostgreSQL no publica ningún puerto hacia Internet en `docker-compose.prod.yml`. El navegador no accede directamente a la base de datos.

## Controles implementados

### Autenticación

- JWT firmado con HMAC.
- `JWT_SECRET` obligatorio en producción.
- El backend rechaza secretos JWT de menos de 32 caracteres.
- Vida útil por defecto del JWT: 2 horas.
- Vida útil permitida: entre 5 minutos y 7 días.
- Login devuelve un mensaje genérico para credenciales incorrectas.
- Nuevas contraseñas: entre 12 y 64 caracteres.
- BCrypt con factor de trabajo 12.
- Flood general del endpoint de login limitado por IP/cliente.
- Un mismo usuario/IP queda temporalmente bloqueado tras 10 credenciales fallidas en 15 minutos.
- Un login correcto reinicia el contador de fallos para esa combinación usuario/IP.

### Autorización

- Roles `USER` y `ADMIN`.
- Endpoints administrativos protegidos en backend con Spring Security.
- Los pedidos comprueban propietario o ADMIN en backend.
- Los participantes de rutas y eventos solo pueden listarse como ADMIN.
- Las fotos solo pueden borrarlas su propietario o un ADMIN.
- Las rutas, eventos y circuitos en borrador no se exponen públicamente.

Las pruebas automáticas incluyen intentos de acceso de un usuario a pedidos ajenos y accesos USER a endpoints ADMIN.

### Rate limiting

Se limitan:

- login,
- registro,
- creación de sesiones de Stripe Checkout,
- uploads,
- bajas de notificaciones.

Caddy sobrescribe los headers `X-Forwarded-*` antes de reenviar al backend y Spring procesa esos headers para que los límites utilicen la IP original del cliente.

### Uploads

- Máximo 5 MB por imagen.
- Máximo 6 MB por petición en el proxy.
- Solo PNG, JPEG y WebP.
- Validación mediante firma/magic bytes y comparación con MIME declarado.
- Nombre físico generado por UUID.
- Los nombres originales no se usan para construir rutas de almacenamiento.
- Protección contra path traversal.
- Límite de fotos por usuario y ruta.
- Limpieza de archivos si falla la transacción.

### Navegador y HTTPS

Caddy añade:

- Content Security Policy.
- HSTS.
- X-Content-Type-Options.
- X-Frame-Options.
- Referrer-Policy.
- Permissions-Policy.
- Cross-Origin-Opener-Policy.
- Cross-Origin-Resource-Policy.

La CSP bloquea scripts de terceros y objetos embebidos por defecto.

### CORS

En producción solo se aceptan orígenes HTTPS explícitos.

No se permite `*`.

### Stripe

- Los importes se calculan en backend.
- El frontend no puede marcar un pedido como pagado.
- El webhook verifica la firma de Stripe.
- Se comprueba TEST/LIVE.
- Se limita el tamaño del webhook.
- Los eventos se procesan de forma idempotente.
- Las claves Stripe no se guardan en el repositorio.

### Errores

- Los errores internos no devuelven stack traces al cliente.
- Los errores inesperados se registran en servidor y devuelven un mensaje genérico.
- Los errores de autorización no revelan datos del recurso.

### Dependencias y CI

- GitHub Actions ejecuta tests de backend y build de frontend.
- Las credenciales de checkout no persisten en el workspace de CI.
- Dependabot revisa semanalmente Maven, npm y GitHub Actions.

## RLS

No se ha activado PostgreSQL Row Level Security en esta fase.

La razón es arquitectónica: Spring Boot utiliza un pool de conexiones con una identidad de base de datos compartida. Una RLS basada únicamente en ese usuario de base de datos no distingue entre usuarios de Guateque Riders.

Para que RLS aporte una segunda barrera real habría que propagar de forma segura la identidad de la petición a cada transacción PostgreSQL, por ejemplo mediante una variable de sesión local controlada por el backend, y diseñar políticas por tabla. Hacerlo parcialmente daría una falsa sensación de seguridad.

Actualmente la autorización se aplica en Spring Security y en los servicios de dominio, y PostgreSQL no es accesible desde el navegador ni desde Internet.

## Riesgo residual: JWT en localStorage

El frontend sigue guardando el JWT en `localStorage`.

La CSP estricta reduce la superficie de XSS, pero un XSS ejecutado en el mismo origen podría acceder al token.

Una migración futura recomendada es:

1. cookie `HttpOnly`, `Secure` y `SameSite`,
2. protección CSRF habilitada,
3. token CSRF asociado a las peticiones mutables,
4. eliminación completa del JWT del almacenamiento accesible por JavaScript.

No se debe migrar a cookies desactivando CSRF.

## Pendientes recomendados antes de una exposición de mayor riesgo

- Comprobación de contraseñas contra listas de credenciales comprometidas.
- Recuperación/cambio de contraseña seguro.
- MFA para cuentas ADMIN si la administración crece.
- Monitorización y alertas de errores/autenticación en producción.
- Backup externo automatizado y prueba periódica de restauración.
- Prueba de penetración externa si la plataforma empieza a manejar un volumen significativo de pagos o usuarios.
