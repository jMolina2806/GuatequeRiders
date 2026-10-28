# Revisión legal previa a producción

Este documento resume los puntos que deben validarse antes de publicar Guateque Riders. No sustituye una revisión jurídica profesional cuando resulte necesaria.

## Identificación del prestador

La LSSI exige que el prestador permita acceder de forma permanente, fácil, directa y gratuita a información identificativa como nombre o denominación, domicilio, email, NIF y, cuando corresponda, datos registrales.

Antes de producción deben completarse esos datos en:

- Aviso legal.
- Política de privacidad.
- Condiciones de Merch.

Referencia:
- Ley 34/2002, artículo 10: https://www.boe.es/buscar/act.php?id=BOE-A-2002-13758

## Información de privacidad

Cuando los datos se obtienen de la persona interesada debe facilitarse, entre otra información:

- identidad y contacto del responsable,
- finalidades,
- bases jurídicas,
- destinatarios,
- conservación,
- derechos,
- transferencias internacionales cuando existan.

Referencias:
- RGPD, artículo 13: https://eur-lex.europa.eu/eli/reg/2016/679/oj
- AEPD, derecho de información: https://www.aepd.es/derechos-y-deberes/conoce-tus-derechos/derecho-de-informacion

## Comunicaciones por email

Los avisos generales de Merch, rutas, eventos y circuitos se mantienen desactivados por defecto y requieren activación voluntaria.

La baja debe ser sencilla y gratuita. La aplicación incluye:

- preferencias por categoría,
- baja desde el perfil,
- enlace de baja en los emails generales,
- confirmación mediante token firmado.

Referencia:
- Ley 34/2002, artículos 21 y 22: https://www.boe.es/buscar/act.php?id=BOE-A-2002-13758

## Merch y desistimiento

Con carácter general, los contratos a distancia con consumidores tienen derecho de desistimiento durante 14 días naturales, salvo excepciones legales.

Los bienes confeccionados conforme a especificaciones del consumidor o claramente personalizados pueden estar exceptuados. Esta excepción no debe aplicarse automáticamente a productos no personalizados.

Referencias:
- Real Decreto Legislativo 1/2007, artículo 102.
- Real Decreto Legislativo 1/2007, artículo 103.c.
- https://www.boe.es/buscar/act.php?id=BOE-A-2007-20555

## Cookies y almacenamiento

La versión actual no integra analítica ni publicidad propia.

Se utiliza almacenamiento local para mantener la sesión autenticada. Si se añaden tecnologías no estrictamente necesarias, deberá revisarse la necesidad de consentimiento previo y controles de configuración.

Referencia:
- Ley 34/2002, artículo 22.2: https://www.boe.es/buscar/act.php?id=BOE-A-2002-13758

## Proveedores

Antes de producción debe documentarse la configuración real de:

- hosting y base de datos,
- correo transaccional,
- Stripe,
- proveedor de merchandising,
- logística si existe.

Debe comprobarse qué proveedores actúan como encargados del tratamiento, sus contratos y, cuando proceda, las garantías para transferencias internacionales.

## Evidencia de aceptación

La aplicación registra:

- fecha y versión de la Política de privacidad reconocida al crear una cuenta,
- fecha y versión de las Condiciones de Merch aceptadas al crear un pedido.

Las versiones se definen en:
`apps/api/src/main/java/com/guatequeriders/api/legal/LegalVersions.java`

Cuando cambie materialmente un documento, debe actualizarse su versión y valorarse si es necesario solicitar una nueva aceptación.
