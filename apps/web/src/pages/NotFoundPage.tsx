import './NotFoundPage.css'

export default function NotFoundPage() {
  return (
    <section className="not-found-page">
      <span className="not-found-page__code">Error / 404</span>
      <h1>Página fuera de ruta.</h1>
      <p>La dirección que buscas no existe o ya no está disponible.</p>
    </section>
  )
}
