# Notificaciones por email

## Desarrollo local

Mailpit se levanta con Docker Compose y captura todos los emails sin enviarlos a Internet.

- SMTP: `localhost:1025`
- Bandeja web: `http://localhost:8025`

`Run Demo Stripe` activa automáticamente el correo local.

Para arrancar sin Stripe:

1. Añade a `.env`:
   ```
   MAIL_ENABLED=true
   MAIL_HOST=localhost
   MAIL_PORT=1025
   MAIL_SMTP_AUTH=false
   MAIL_STARTTLS=false
   MAIL_FROM="Guateque Riders <no-reply@guatequeriders.local>"
   APP_PUBLIC_URL=http://localhost:5173
   ```
2. Ejecuta las tareas `Run Backend` y `Run Frontend`.
3. Abre `http://localhost:8025`.

## Avisos implementados

### Merch

- Apertura de campaña: todos los usuarios registrados.
- Pago confirmado: comprador.
- Pedido en producción: comprador.
- Pedido listo para entregar: comprador.
- Pedido entregado: comprador.

### Rutas

- Publicación: todos los usuarios registrados.
- Inscripción: usuario inscrito.
- Cambios mientras está publicada: participantes inscritos.
- Cancelación: participantes inscritos.

### Eventos

- Publicación: todos los usuarios registrados.
- Inscripción: usuario inscrito.
- Cambios mientras está publicado: participantes inscritos.
- Cierre o cancelación: participantes inscritos.

### Circuitos

- Publicación: todos los usuarios registrados.
- Cambios mientras está publicado: todos los usuarios registrados.
- Cancelación de una actividad publicada: todos los usuarios registrados.

## Producción

El código usa SMTP estándar. Configura el proveedor mediante variables de entorno:

```
MAIL_ENABLED=true
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_SMTP_AUTH=true
MAIL_STARTTLS=true
MAIL_USERNAME=...
MAIL_PASSWORD=...
MAIL_FROM="Guateque Riders <no-reply@tu-dominio.es>"
APP_PUBLIC_URL=https://tu-dominio.es
```

Las credenciales no deben almacenarse en Git.
