# Revisión legal previa a producción

Este documento resume los puntos que deben validarse antes de publicar Guateque Riders. No sustituye una revisión jurídica profesional cuando resulte necesaria.

## Naturaleza de Guateque Riders

Guateque Riders se plantea actualmente como una comunidad motera, no como sociedad mercantil, empresa o asociación formalizada.

Por tanto, la web no debe presentar a Guateque Riders como si fuera una entidad jurídica inexistente ni inventar CIF, domicilio social o datos registrales.

Lo que sí debe definirse antes de abrir ciertas funciones al público es quién actúa realmente en cada papel:

- responsable de la plataforma,
- responsable del tratamiento de datos personales,
- y, solo si se activa la venta real de Merch, quién actúa jurídicamente como vendedor o responsable de esa operación.

## Aviso legal y actividad económica

La LSSI exige determinada información al prestador cuando resulte aplicable a un servicio de la sociedad de la información, especialmente si existe actividad económica.

Si Guateque Riders activa venta real de Merch u otra actividad económica, deberá revisarse el aviso legal con la situación real de la persona o entidad que preste ese servicio, incluyendo la información exigida por el artículo 10 de la LSSI.

No debe publicarse un CIF, domicilio social o inscripción registral de Guateque Riders mientras tales datos no existan.

Referencia:
- Ley 34/2002, artículo 10: https://www.boe.es/buscar/act.php?id=BOE-A-2002-13758

## Información de privacidad

El RGPD sí exige identificar al responsable del tratamiento cuando se recogen datos personales.

Antes de abrir el registro público de usuarios debe definirse quién decide realmente los fines y medios del tratamiento de los datos de la comunidad y publicar al menos su identidad y un medio de contacto adecuado.

La política debe informar, entre otros puntos, sobre:

- identidad y contacto del responsable,
- finalidades,
- bases jurídicas,
- destinatarios,
- conservación,
- derechos,
- transferencias internacionales cuando existan.

Referencias:
- RGPD, artículo 13: https://eur-lex.europa.eu/eli/reg/2016/679/oj
- AEPD, responsable y encargado: https://www.aepd.es/preguntas-frecuentes/2-tus-obligaciones-como-responsable-del-tratamiento/8-responsable-y-encargado-del-tratamiento/FAQ-0251-como-se-si-soy-responsable-o-encargado
- AEPD, ejercicio de derechos: https://www.aepd.es/preguntas-frecuentes/17-internet-y-redes-sociales/FAQ-1703-donde-me-debo-dirigir-para-poder-ejercer-un-derecho-en-proteccion-de-datos

## Merch

Mientras Stripe LIVE esté desactivado, el Merch puede mantenerse como flujo técnico de prueba.

Antes de aceptar dinero real deben definirse:

- quién recibe los pagos,
- quién actúa como vendedor o responsable de la operación,
- canal de contacto para pedidos e incidencias,
- modalidad y costes de entrega,
- condiciones de cancelación, devolución y desistimiento,
- tratamiento de productos personalizados.

Solo en ese momento deben completarse las obligaciones de información que correspondan a la situación jurídica real.

Referencias:
- Real Decreto Legislativo 1/2007, artículos 102 y 103.
- https://www.boe.es/buscar/act.php?id=BOE-A-2007-20555

## Comunicaciones por email

Los avisos generales de Merch, rutas, eventos y circuitos se mantienen desactivados por defecto y requieren activación voluntaria.

La aplicación incluye:

- preferencias por categoría,
- baja desde el perfil,
- enlace de baja en los emails generales,
- confirmación mediante token firmado.

Referencia:
- Ley 34/2002, artículos 21 y 22: https://www.boe.es/buscar/act.php?id=BOE-A-2002-13758

## Cookies y almacenamiento

La versión actual no integra analítica ni publicidad propia.

Se utiliza almacenamiento local para mantener la sesión autenticada. Si se añaden tecnologías no estrictamente necesarias, deberá revisarse la necesidad de consentimiento previo y controles de configuración.

Referencia:
- Ley 34/2002, artículo 22.2: https://www.boe.es/buscar/act.php?id=BOE-A-2002-13758

## Proveedores

Antes de producción debe documentarse la configuración real de:

- hosting y base de datos,
- correo transaccional,
- Stripe si se activa,
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
