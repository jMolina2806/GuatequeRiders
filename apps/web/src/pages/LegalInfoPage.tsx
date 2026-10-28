import { useLocation } from 'react-router-dom'
import './LegalInfoPage.css'

type LegalBlock = {
  title: string
  paragraphs?: string[]
  items?: string[]
}

type LegalSection = {
  eyebrow: string
  title: string
  intro: string
  requiresIdentity?: boolean
  blocks: LegalBlock[]
}

const IDENTITY_NOTICE =
  'Antes de publicar en producción deben sustituirse los campos entre corchetes por los datos reales del titular de Guateque Riders.'

const SECTIONS: Record<string, LegalSection> = {
  '/legal': {
    eyebrow: 'Información del sitio',
    title: 'Aviso legal',
    intro:
      'Información general sobre el titular, el uso de la plataforma y las condiciones de acceso a Guateque Riders.',
    requiresIdentity: true,
    blocks: [
      {
        title: '1. Titular del sitio',
        items: [
          'Titular o denominación social: [PENDIENTE DE COMPLETAR]',
          'NIF/CIF: [PENDIENTE DE COMPLETAR]',
          'Domicilio: [PENDIENTE DE COMPLETAR]',
          'Email de contacto legal: [PENDIENTE DE COMPLETAR]',
          'Datos registrales, si resultan aplicables: [PENDIENTE DE COMPLETAR]',
        ],
      },
      {
        title: '2. Objeto',
        paragraphs: [
          'Guateque Riders es una plataforma destinada a la comunidad motera para consultar y gestionar rutas, eventos, actividades de circuito, colaboradores y campañas de merchandising.',
          'Determinadas funciones requieren una cuenta de usuario. Las funciones de administración están reservadas a las personas autorizadas por Guateque Riders.',
        ],
      },
      {
        title: '3. Uso de la plataforma',
        items: [
          'La persona usuaria debe facilitar información veraz y mantener la confidencialidad de sus credenciales.',
          'No está permitido utilizar la plataforma para actividades ilícitas, fraudulentas, abusivas o que perjudiquen a otras personas o a la propia comunidad.',
          'Guateque Riders podrá limitar o suspender accesos cuando sea necesario para proteger la seguridad, el funcionamiento de la plataforma o el cumplimiento de estas condiciones.',
        ],
      },
      {
        title: '4. Contenidos y propiedad intelectual',
        paragraphs: [
          'Los textos, diseños, marcas, logotipos, fotografías y demás contenidos estarán sujetos a los derechos de sus respectivos titulares. Su disponibilidad en la plataforma no implica una cesión de derechos salvo indicación expresa.',
          'Quien aporte contenidos a la plataforma, como fotografías de rutas, declara disponer de los derechos o autorizaciones necesarios para su publicación.',
        ],
      },
      {
        title: '5. Enlaces y servicios externos',
        paragraphs: [
          'La plataforma puede enlazar con servicios externos, entre ellos Instagram, WhatsApp y proveedores de pago. Guateque Riders no controla sus contenidos, disponibilidad ni políticas de privacidad.',
        ],
      },
      {
        title: '6. Disponibilidad y seguridad',
        paragraphs: [
          'Se aplican medidas técnicas razonables para mantener la seguridad y disponibilidad del servicio. No obstante, no puede garantizarse una disponibilidad ininterrumpida ni la ausencia absoluta de incidencias técnicas.',
        ],
      },
      {
        title: '7. Legislación aplicable',
        paragraphs: [
          'Este aviso se interpretará conforme a la normativa española y de la Unión Europea que resulte aplicable, sin perjuicio de los derechos imperativos de las personas consumidoras y usuarias.',
        ],
      },
    ],
  },
  '/privacy': {
    eyebrow: 'Protección de datos',
    title: 'Política de privacidad',
    intro:
      'Esta política explica qué datos trata Guateque Riders, para qué se utilizan y qué derechos tienes.',
    requiresIdentity: true,
    blocks: [
      {
        title: '1. Responsable del tratamiento',
        items: [
          'Responsable: [PENDIENTE DE COMPLETAR]',
          'NIF/CIF: [PENDIENTE DE COMPLETAR]',
          'Domicilio: [PENDIENTE DE COMPLETAR]',
          'Email para privacidad y ejercicio de derechos: [PENDIENTE DE COMPLETAR]',
        ],
      },
      {
        title: '2. Datos que tratamos',
        items: [
          'Cuenta: nombre de usuario, email, contraseña almacenada de forma cifrada y fecha de alta.',
          'Participación: inscripciones y actividad relacionada con rutas y eventos.',
          'Merch: nombre, email, teléfono, productos, talla, personalización, notas y, cuando proceda, datos necesarios para entrega.',
          'Pagos: estado, identificadores técnicos e importe. Los datos completos de tarjeta se introducen en Stripe y no se almacenan en Guateque Riders.',
          'Notificaciones: preferencias de email y constancia de altas o bajas por categoría.',
          'Contenido aportado: por ejemplo, fotografías subidas a galerías de rutas y los datos técnicos asociados a su gestión.',
        ],
      },
      {
        title: '3. Finalidades y bases jurídicas',
        items: [
          'Crear y mantener la cuenta, autenticar al usuario y prestar las funciones solicitadas: ejecución de la relación con la persona usuaria.',
          'Gestionar inscripciones, pedidos, pagos, producción, entrega e incidencias: ejecución del servicio o contrato solicitado.',
          'Conservar la información exigida por normativa fiscal, contable, de consumo u otras obligaciones aplicables: cumplimiento de obligaciones legales.',
          'Enviar comunicaciones operativas sobre pedidos, pagos, inscripciones o cambios relevantes: ejecución del servicio solicitado.',
          'Enviar avisos generales de Merch, rutas, eventos o circuitos: consentimiento, que puede activarse o retirarse por categoría desde el perfil.',
          'Proteger la plataforma frente a fraude, abuso e incidentes de seguridad: interés legítimo en mantener la seguridad del servicio, cuando resulte aplicable.',
        ],
      },
      {
        title: '4. Destinatarios',
        paragraphs: [
          'Los datos podrán ser tratados por proveedores que presten servicios necesarios de alojamiento, base de datos, correo electrónico o soporte técnico bajo las condiciones contractuales correspondientes.',
          'Para pagos con tarjeta se utiliza Stripe Checkout. Para preparar o entregar pedidos podrán comunicarse únicamente los datos necesarios a proveedores o servicios logísticos cuando resulte imprescindible.',
          'No se venderán datos personales a terceros.',
        ],
      },
      {
        title: '5. Transferencias internacionales',
        paragraphs: [
          'Si alguno de los proveedores utilizados trata datos fuera del Espacio Económico Europeo, se comprobará que exista un mecanismo válido de transferencia internacional y las garantías exigidas por la normativa aplicable.',
        ],
      },
      {
        title: '6. Conservación',
        items: [
          'Los datos de cuenta se conservarán mientras la cuenta permanezca activa y posteriormente durante el tiempo necesario para atender responsabilidades legales.',
          'Los datos de pedidos y pagos se conservarán durante los plazos exigidos por la normativa aplicable y para atender incidencias o reclamaciones.',
          'Las preferencias de comunicaciones se conservarán mientras sean necesarias para respetar la elección del usuario.',
          'Los contenidos aportados se conservarán mientras formen parte de la funcionalidad correspondiente o hasta que proceda su retirada.',
        ],
      },
      {
        title: '7. Derechos',
        paragraphs: [
          'Puedes solicitar acceso, rectificación, supresión, oposición, limitación y portabilidad cuando correspondan, así como retirar un consentimiento sin afectar a la licitud del tratamiento anterior.',
          'Las solicitudes deberán enviarse al email de privacidad indicado en esta política, una vez completado el dato del responsable. También puedes presentar una reclamación ante la Agencia Española de Protección de Datos.',
        ],
      },
      {
        title: '8. Almacenamiento técnico en el navegador',
        paragraphs: [
          'La aplicación utiliza almacenamiento local del navegador para mantener la sesión iniciada y la información técnica necesaria de autenticación. Consulta la sección de Cookies y almacenamiento para más información.',
        ],
      },
    ],
  },
  '/merch-terms': {
    eyebrow: 'Merchandising',
    title: 'Condiciones de Merch',
    intro:
      'Condiciones aplicables a las campañas y pedidos de merchandising gestionados mediante Guateque Riders.',
    requiresIdentity: true,
    blocks: [
      {
        title: '1. Vendedor y contacto',
        items: [
          'Titular responsable de la venta: [PENDIENTE DE COMPLETAR]',
          'NIF/CIF: [PENDIENTE DE COMPLETAR]',
          'Domicilio: [PENDIENTE DE COMPLETAR]',
          'Email para pedidos, desistimiento e incidencias: [PENDIENTE DE COMPLETAR]',
        ],
      },
      {
        title: '2. Funcionamiento de las campañas',
        paragraphs: [
          'El Merch funciona por campañas con un periodo de apertura. Mientras la campaña esté abierta pueden crearse pedidos y completarse pagos. Al cerrarse se congela el lote que se utilizará para preparar el pedido al proveedor.',
          'Un pedido no se considera pagado hasta que Guateque Riders recibe la confirmación válida del proveedor de pago.',
        ],
      },
      {
        title: '3. Productos, tallas y personalización',
        paragraphs: [
          'La ficha del producto y el resumen previo al pago muestran el producto, talla, cantidad, personalización e importe seleccionado.',
          'La persona compradora es responsable de revisar talla y texto de personalización antes de confirmar el pedido. Una personalización solicitada por el usuario puede convertir el producto en un bien confeccionado conforme a sus especificaciones.',
        ],
      },
      {
        title: '4. Precio y pago',
        paragraphs: [
          'El importe total aplicable se muestra antes de abrir Stripe Checkout. Cualquier impuesto o coste adicional que legalmente corresponda deberá estar incluido o identificado antes de finalizar la compra.',
          'Los pagos con tarjeta se procesan mediante Stripe Checkout. Guateque Riders no recibe ni almacena el número completo de tarjeta ni el CVC.',
        ],
      },
      {
        title: '5. Confirmación y seguimiento',
        paragraphs: [
          'Tras confirmarse el pago se registra el pedido y se envía una comunicación operativa al email indicado. El estado puede avanzar por las fases Confirmado, En producción, Listo para entregar y Entregado.',
          'La versión de estas condiciones aceptada al crear el pedido queda registrada junto con la fecha de aceptación.',
        ],
      },
      {
        title: '6. Cancelación y desistimiento',
        paragraphs: [
          'La aplicación permite cancelar directamente un pedido pendiente que todavía no haya sido pagado. Esta limitación técnica no reduce los derechos legales de la persona consumidora.',
          'Con carácter general, en las ventas a distancia a consumidores existe un plazo legal de desistimiento, salvo las excepciones previstas por la normativa.',
          'El derecho de desistimiento puede no resultar aplicable a bienes confeccionados conforme a las especificaciones de la persona consumidora o claramente personalizados. La excepción se aplicará únicamente cuando el producto concreto cumpla legalmente esos requisitos.',
          'Los productos no personalizados mantienen los derechos de desistimiento y devolución que resulten aplicables. Los derechos por falta de conformidad o producto defectuoso no quedan afectados por la personalización.',
        ],
      },
      {
        title: '7. Entrega',
        paragraphs: [
          'La modalidad y condiciones de entrega deberán comunicarse antes de finalizar la compra. Si se habilitan envíos, sus costes y condiciones se mostrarán previamente. Cuando la entrega se coordine de forma presencial, se informará al comprador cuando el pedido esté listo.',
        ],
      },
      {
        title: '8. Incidencias',
        paragraphs: [
          'Las incidencias de pago, producción, entrega, talla, personalización o producto deberán comunicarse al email de pedidos indicado en estas condiciones. La respuesta se realizará respetando los derechos que correspondan conforme a la normativa de consumo.',
        ],
      },
    ],
  },
  '/cookies': {
    eyebrow: 'Navegación',
    title: 'Cookies y almacenamiento',
    intro:
      'Información sobre el almacenamiento técnico utilizado actualmente por Guateque Riders.',
    blocks: [
      {
        title: '1. Situación actual',
        paragraphs: [
          'Guateque Riders no incorpora actualmente herramientas propias de analítica, publicidad comportamental ni cookies de marketing.',
          'La aplicación utiliza almacenamiento local del navegador para conservar el token de autenticación y los datos mínimos de sesión. Este almacenamiento es necesario para mantener la sesión iniciada y prestar funciones solicitadas por el usuario.',
        ],
      },
      {
        title: '2. Servicios externos',
        paragraphs: [
          'Al abandonar Guateque Riders para utilizar servicios externos, como Stripe Checkout, Instagram o WhatsApp, esos servicios pueden aplicar sus propias cookies o tecnologías de almacenamiento conforme a sus políticas.',
        ],
      },
      {
        title: '3. Cambios futuros',
        paragraphs: [
          'Si en el futuro se incorporan cookies o tecnologías no estrictamente necesarias, la plataforma mostrará la información y los controles de consentimiento que resulten exigibles antes de utilizarlas.',
        ],
      },
    ],
  },
}

export default function LegalInfoPage() {
  const { pathname } = useLocation()
  const section = SECTIONS[pathname] ?? SECTIONS['/legal']

  return (
    <section className="legal-info">
      <header className="legal-info__header">
        <span>{section.eyebrow}</span>
        <h1>{section.title}</h1>
        <p>{section.intro}</p>
        <small>Versión: 28/10/2026</small>
      </header>

      {section.requiresIdentity && (
        <div className="legal-info__notice">
          <strong>Datos obligatorios pendientes</strong>
          <p>{IDENTITY_NOTICE}</p>
        </div>
      )}

      <div className="legal-info__content">
        {section.blocks.map((block) => (
          <article className="legal-info__block" key={block.title}>
            <h2>{block.title}</h2>
            {block.paragraphs?.map((paragraph) => <p key={paragraph}>{paragraph}</p>)}
            {block.items && (
              <ul>
                {block.items.map((item) => <li key={item}>{item}</li>)}
              </ul>
            )}
          </article>
        ))}
      </div>

      <div className="legal-info__reference">
        <strong>Revisión antes de producción</strong>
        <p>
          Estos textos están preparados para la funcionalidad actual de la plataforma. Antes de publicar deben completarse los datos identificativos del titular y revisarse contra la configuración real de dominio, hosting, correo, pagos, proveedores y entrega.
        </p>
      </div>
    </section>
  )
}
