import { useLocation } from 'react-router-dom'
import './LegalInfoPage.css'

type LegalSection = {
  eyebrow: string
  title: string
  description: string
  points: string[]
}

const SECTIONS: Record<string, LegalSection> = {
  '/legal': {
    eyebrow: 'Información del sitio',
    title: 'Aviso legal',
    description:
      'Esta sección está preparada para incorporar la información identificativa y legal definitiva de Guateque Riders antes de la publicación oficial.',
    points: [
      'Identificación del responsable del sitio.',
      'Datos de contacto y medio de comunicación.',
      'Condiciones generales de uso de la web.',
    ],
  },
  '/privacy': {
    eyebrow: 'Protección de datos',
    title: 'Privacidad',
    description:
      'La política definitiva detallará cómo se tratan los datos de usuarios, participantes y pedidos antes de abrir la web al público.',
    points: [
      'Responsable y finalidades del tratamiento.',
      'Base jurídica, conservación y destinatarios.',
      'Derechos de las personas usuarias y forma de ejercerlos.',
    ],
  },
  '/merch-terms': {
    eyebrow: 'Merchandising',
    title: 'Condiciones Merch',
    description:
      'La versión definitiva recogerá las condiciones aplicables a campañas, pedidos, personalizaciones, pagos y entrega del merchandising.',
    points: [
      'Funcionamiento y cierre de las campañas.',
      'Personalizaciones, tallas y confirmación del pedido.',
      'Pagos, incidencias, cancelaciones y entrega.',
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
        <p>{section.description}</p>
      </header>

      <div className="legal-info__notice">
        <strong>Borrador interno</strong>
        <p>
          Este contenido todavía no constituye la versión legal definitiva. Se completará y revisará antes de activar el servicio en producción.
        </p>
      </div>

      <div className="legal-info__content">
        <h2>Contenido previsto</h2>
        <ul>
          {section.points.map((point) => (
            <li key={point}>{point}</li>
          ))}
        </ul>
      </div>
    </section>
  )
}
