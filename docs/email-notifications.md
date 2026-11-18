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

## Preferencias y baja

Los usuarios pueden activar o desactivar desde su perfil:

- Merch.
- Rutas.
- Eventos.
- Circuitos.

Estas preferencias afectan a avisos generales como nuevas publicaciones.

Los correos operativos necesarios sobre pagos, pedidos, inscripciones y cambios o cancelaciones de actividades en las que el usuario ya participa se mantienen separados.

Los correos generales incluyen:

- `Gestionar notificaciones`: abre las preferencias del perfil.
- `Dejar de recibir estos avisos`: abre una página de confirmación de baja para esa categoría.

La baja utiliza un token firmado. El enlace por sí solo no modifica preferencias; la baja se confirma mediante una petición POST desde la página de confirmación.

## Avisos implementados

### Merch

- Apertura de campaña: usuarios con avisos de Merch activos.
- Pago confirmado: comprador.
- Pedido en producción: comprador.
- Pedido listo para entregar: comprador.
- Pedido entregado: comprador.

### Rutas

- Publicación: usuarios con avisos de Rutas activos.
- Inscripción: usuario inscrito.
- Cambios mientras está publicada: participantes inscritos.
- Cancelación: participantes inscritos.

### Eventos

- Publicación: usuarios con avisos de Eventos activos.
- Inscripción: usuario inscrito.
- Cambios mientras está publicado: participantes inscritos.
- Cierre o cancelación: participantes inscritos.

### Circuitos

- Publicación: usuarios con avisos de Circuitos activos.
- Cambios mientras está publicado: usuarios con avisos de Circuitos activos.
- Cancelación de una actividad publicada: usuarios con avisos de Circuitos activos.

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
UNSUBSCRIBE_SECRET=...
```

`UNSUBSCRIBE_SECRET` debe ser largo, aleatorio y mantenerse privado. Puede generarse, por ejemplo, con:

```
openssl rand -hex 32
```

Para un proveedor SMTP como Resend, usa el host, puerto, usuario y API key indicados por el proveedor y un remitente perteneciente a un dominio verificado.

Las credenciales no deben almacenarse en Git.
