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
  requiresDefinition?: boolean
  blocks: LegalBlock[]
}

const DEFINITION_NOTICE =
  'Guateque Riders es una comunidad motera, no una empresa ni una sociedad mercantil. Antes de abrir la plataforma al público deberá definirse quién actúa como responsable de la plataforma y del tratamiento de datos. Si se activa la venta real de Merch, también deberá definirse quién realiza jurídicamente esa venta y recibe los pagos.'

const SECTIONS: Record<string, LegalSection> = {
  '/legal': {
    eyebrow: 'Información del sitio',
    title: 'Aviso legal',
    intro:
      'Información general sobre Guateque Riders, el uso de la plataforma y las condiciones de acceso.',
    requiresDefinition: true,
    blocks: [
      {
        title: '1. Sobre Guateque Riders',
        paragraphs: [
          'Guateque Riders es una comunidad motera y esta plataforma se ha creado para facilitar la organización de rutas, eventos, actividades de circuito, colaboradores, comunidad y merchandising.',
          'Guateque Riders no se presenta en esta versión como sociedad mercantil, empresa o asociación formalizada.',
        ],
        items: [
          'Responsable de la plataforma: [PENDIENTE DE DEFINIR]',
          'Email general de contacto: [PENDIENTE HASTA DISPONER DE DOMINIO]',
        ],
      },
      {
        title: '2. Objeto',
        paragraphs: [
          'La plataforma permite consultar y gestionar rutas, eventos, actividades de circuito, colaboradores y campañas de merchandising.',
          'Determinadas funciones requieren una cuenta de usuario. Las funciones de administración están reservadas a las personas autorizadas por la comunidad.',
        ],
      },
      {
        title: '3. Uso de la plataforma',
        items: [
          'La persona usuaria debe facilitar información veraz y mantener la confidencialidad de sus credenciales.',
          'No está permitido utilizar la plataforma para actividades ilícitas, fraudulentas, abusivas o que perjudiquen a otras personas o a la comunidad.',
          'El acceso podrá limitarse o suspenderse cuando sea necesario para proteger la seguridad, el funcionamiento de la plataforma o a sus usuarios.',
        ],
      },
      {
        title: '4. Contenidos y propiedad intelectual',
        paragraphs: [
          'Los textos, diseños, marcas, logotipos, fotografías y demás contenidos estarán sujetos a los derechos de sus respectivos titulares. Su disponibilidad en la plataforma no implica una cesión de derechos salvo indicación expresa.',
          'Quien aporte contenidos a la plataforma, como fotografías de rutas, debe disponer de los derechos o autorizaciones necesarios para su publicación.',
        ],
      },
      {
        title: '5. Enlaces y servicios externos',
        paragraphs: [
          'La plataforma puede enlazar con servicios externos como Instagram, WhatsApp o proveedores de pago. Cada servicio externo se rige por sus propias condiciones y políticas.',
        ],
      },
      {
        title: '6. Disponibilidad y seguridad',
        paragraphs: [
          'Se aplican medidas técnicas razonables para mantener la seguridad y disponibilidad del servicio. No puede garantizarse una disponibilidad ininterrumpida ni la ausencia absoluta de incidencias técnicas.',
        ],
      },
      {
        title: '7. Actividad económica futura',
        paragraphs: [
          'Si en el futuro la plataforma desarrolla una actividad económica real, por ejemplo mediante venta de Merch con pagos LIVE, este aviso deberá revisarse y completarse con la identificación y demás información legal que corresponda a la persona o entidad que preste ese servicio.',
        ],
      },
    ],
  },
  '/privacy': {
    eyebrow: 'Protección de datos',
    title: 'Política de privacidad',
    intro:
      'Esta política explica qué datos trata la plataforma de Guateque Riders, para qué se utilizan y qué derechos tienes.',
    requiresDefinition: true,
    blocks: [
      {
        title: '1. Responsable del tratamiento',
        paragraphs: [
          'Guateque Riders es una comunidad motera. La persona que finalmente determine los fines y medios del tratamiento de los datos deberá identificarse como responsable antes de abrir el registro público de usuarios.',
        ],
        items: [
          'Responsable del tratamiento: [PENDIENTE DE DEFINIR]',
          'Email para privacidad y ejercicio de derechos: [PENDIENTE HASTA DISPONER DE DOMINIO]',
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
          'Crear y mantener la cuenta, autenticar al usuario y prestar las funciones solicitadas.',
          'Gestionar inscripciones, pedidos, pagos, producción, entrega e incidencias cuando esas funciones estén activas.',
          'Enviar comunicaciones operativas sobre pedidos, pagos, inscripciones o cambios relevantes.',
          'Enviar avisos generales de Merch, rutas, eventos o circuitos únicamente cuando el usuario los haya activado.',
          'Proteger la plataforma frente a fraude, abuso e incidentes de seguridad.',
        ],
      },
      {
        title: '4. Destinatarios',
        paragraphs: [
          'Los datos podrán ser tratados por proveedores necesarios para alojamiento, base de datos, correo electrónico o soporte técnico bajo las condiciones que correspondan.',
          'Si se activan pagos reales, Stripe procesará el pago mediante Checkout. Para preparar o entregar pedidos podrán comunicarse únicamente los datos necesarios al proveedor o servicio logístico correspondiente.',
          'No se venderán datos personales a terceros.',
        ],
      },
      {
        title: '5. Transferencias internacionales',
        paragraphs: [
          'Si alguno de los proveedores utilizados trata datos fuera del Espacio Económico Europeo, antes de producción se comprobarán las garantías y mecanismos aplicables.',
        ],
      },
      {
        title: '6. Conservación',
        items: [
          'Los datos de cuenta se conservarán mientras la cuenta permanezca activa y posteriormente durante el tiempo necesario para atender responsabilidades que resulten aplicables.',
          'Los datos de pedidos y pagos se conservarán durante el tiempo necesario para gestionar el servicio y las obligaciones que correspondan si se activa la venta real.',
          'Las preferencias de comunicaciones se conservarán mientras sean necesarias para respetar la elección del usuario.',
          'Los contenidos aportados se conservarán mientras formen parte de la funcionalidad correspondiente o hasta que proceda su retirada.',
        ],
      },
      {
        title: '7. Derechos',
        paragraphs: [
          'Puedes solicitar acceso, rectificación, supresión, oposición, limitación y portabilidad cuando correspondan, así como retirar un consentimiento sin afectar a la licitud del tratamiento anterior.',
          'Antes de abrir el registro público deberá publicarse el email de contacto del responsable para poder ejercer estos derechos. También puede presentarse una reclamación ante la Agencia Española de Protección de Datos.',
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
      'Funcionamiento previsto para las campañas y pedidos de merchandising gestionados mediante Guateque Riders.',
    requiresDefinition: true,
    blocks: [
      {
        title: '1. Situación del Merch',
        paragraphs: [
          'Guateque Riders es una comunidad motera y estas condiciones describen el funcionamiento técnico previsto de las campañas de Merch.',
          'Mientras Stripe LIVE no esté activado, esta funcionalidad se mantiene en entorno de prueba y no constituye por sí sola una oferta pública de venta real.',
          'Antes de aceptar pagos reales deberá definirse quién actúa jurídicamente como vendedor o responsable de la operación, quién recibe los pagos y cuál será el canal de contacto para pedidos e incidencias.',
        ],
        items: [
          'Responsable de la venta real: [PENDIENTE ANTES DE ACTIVAR STRIPE LIVE]',
          'Email para pedidos e incidencias: [PENDIENTE HASTA DISPONER DE DOMINIO]',
        ],
      },
      {
        title: '2. Funcionamiento de las campañas',
        paragraphs: [
          'El Merch funciona por campañas con un periodo de apertura. Mientras la campaña esté abierta pueden crearse pedidos y completarse pagos cuando el sistema de pago esté habilitado. Al cerrarse se congela el lote que se utilizará para preparar el pedido al proveedor.',
          'Un pedido no se considera pagado hasta que la plataforma recibe la confirmación válida del proveedor de pago.',
        ],
      },
      {
        title: '3. Productos, tallas y personalización',
        paragraphs: [
          'La ficha del producto y el resumen previo al pago muestran el producto, talla, cantidad, personalización e importe seleccionado.',
          'La persona compradora es responsable de revisar talla y texto de personalización antes de confirmar el pedido.',
        ],
      },
      {
        title: '4. Precio y pago',
        paragraphs: [
          'Antes de activar una venta real deberán quedar definidos y mostrados de forma clara el precio final, los posibles costes de entrega y cualquier otro importe aplicable.',
          'Cuando se active Stripe LIVE, los pagos con tarjeta se procesarán mediante Stripe Checkout. Guateque Riders no recibirá ni almacenará el número completo de tarjeta ni el CVC.',
        ],
      },
      {
        title: '5. Confirmación y seguimiento',
        paragraphs: [
          'Tras confirmarse un pago se registra el pedido y se envía una comunicación operativa al email indicado. El estado puede avanzar por las fases Confirmado, En producción, Listo para entregar y Entregado.',
          'La versión de estas condiciones aceptada al crear el pedido queda registrada junto con la fecha de aceptación.',
        ],
      },
      {
        title: '6. Cancelación, devolución y personalización',
        paragraphs: [
          'La aplicación permite cancelar directamente un pedido pendiente que todavía no haya sido pagado.',
          'Antes de activar ventas reales deberán concretarse las condiciones de desistimiento, devolución e incidencias según el tipo de producto y la persona o entidad que actúe como vendedor.',
          'Si existen productos claramente personalizados, se revisará específicamente cómo afecta esa personalización al derecho de desistimiento. Los derechos frente a productos defectuosos o no conformes deberán mantenerse en todo caso cuando resulten aplicables.',
        ],
      },
      {
        title: '7. Entrega',
        paragraphs: [
          'Antes de activar ventas reales se definirá si la entrega será presencial, mediante envío o ambas opciones, y se mostrará al usuario cualquier coste y condición antes del pago.',
        ],
      },
      {
        title: '8. Activación de venta real',
        paragraphs: [
          'Stripe LIVE permanecerá desactivado hasta que estén definidos el responsable de la venta, las condiciones reales de entrega y devolución, el canal de contacto y la información legal que corresponda.',
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

      {section.requiresDefinition && (
        <div className="legal-info__notice">
          <strong>Definición pendiente antes de producción</strong>
          <p>{DEFINITION_NOTICE}</p>
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
          Los textos reflejan que Guateque Riders es una comunidad motera. Antes de abrir el registro público deberá identificarse al responsable real del tratamiento de datos. Antes de activar Stripe LIVE deberá definirse además quién realiza la venta de Merch y adaptar la información legal a esa situación concreta.
        </p>
      </div>
    </section>
  )
}
