import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { CATEGORY_LABELS, getNews, type NewsItem, type NewsMedia } from './news.service'
import './news.css'

export function NewsMediaView({ media }: { media: NewsMedia[] }) {
  return <div className="news-media">{media.map((item, index) => item.type === 'IMAGE'
    ? <img key={index} src={item.url} alt="Imagen de la noticia" loading="lazy" />
    : item.url.startsWith('/api/')
      ? <video key={index} controls preload="metadata" src={item.url} />
      : <a key={index} href={item.url} target="_blank" rel="noopener noreferrer">Ver vídeo en YouTube</a>)}</div>
}

export function NewsCards({ items }: { items: NewsItem[] }) {
  return <div className="news-grid">{items.map((item) =>
    <article className="news-card" key={item.id}>
      {item.media.find((media) => media.type === 'IMAGE') &&
        <img className="news-cover" src={item.media.find((media) => media.type === 'IMAGE')!.url} alt="" loading="lazy" />}
      <div className="news-card-content">
        <span className="eyebrow">{CATEGORY_LABELS[item.category]} · {new Date(item.createdAt).toLocaleDateString('es-ES')}</span>
        <h3>{item.title}</h3><p>{item.summary}</p>
        <Link to={`/news/${item.id}`} className="text-link">Leer noticia →</Link>
      </div>
    </article>)}</div>
}

export default function NewsPage() {
  const [items, setItems] = useState<NewsItem[]>([])
  const [error, setError] = useState('')
  const [filter, setFilter] = useState('ALL')
  useEffect(() => { getNews().then(setItems).catch((e: Error) => setError(e.message)) }, [])
  const selected = filter === 'ALL' ? items : items.filter((item) => item.category === filter)
  return <section className="news-page">
    <span className="eyebrow">Actualidad Guateque Riders</span><h1>Noticias y novedades</h1>
    <p>Colaboradores, comunidad, comunicados y contenido multimedia.</p>
    <div className="news-filters">
      <button type="button" onClick={() => setFilter('ALL')} aria-pressed={filter === 'ALL'}>Todas</button>
      {Object.entries(CATEGORY_LABELS).map(([value, label]) =>
        <button type="button" key={value} onClick={() => setFilter(value)} aria-pressed={filter === value}>{label}</button>)}
    </div>
    {error && <p role="alert">{error}</p>}
    {selected.length ? <NewsCards items={selected} /> : !error && <p>No hay noticias publicadas en esta categoría.</p>}
  </section>
}

export function NewsDetailPage() {
  const [items, setItems] = useState<NewsItem[]>([])
  const [error, setError] = useState('')
  useEffect(() => { getNews().then(setItems).catch((e: Error) => setError(e.message)) }, [])
  const id = Number(window.location.pathname.split('/').pop())
  const item = items.find((news) => news.id === id)
  return <article className="news-page news-detail">
    <Link to="/news" className="text-link">← Todas las noticias</Link>
    {error && <p role="alert">{error}</p>}
    {item ? <><span className="eyebrow">{CATEGORY_LABELS[item.category]}</span><h1>{item.title}</h1>
      <p>{item.summary}</p><NewsMediaView media={item.media} />
      <div className="news-body">{item.body}</div></> : !error && <p>Noticia no disponible.</p>}
  </article>
}
