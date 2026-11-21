import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { CATEGORY_LABELS, deleteNews, getAdminNews, saveNews, uploadNewsMedia, type NewsInput, type NewsItem } from './news.service'
import './news.css'

const empty: NewsInput = {
  title: '', summary: '', body: '', category: 'COMMUNITY', media: [],
  published: false, featured: false, publishAt: null,
}
export default function AdminNewsPage() {
  const [items, setItems] = useState<NewsItem[]>([])
  const [editing, setEditing] = useState<number | undefined>()
  const [form, setForm] = useState<NewsInput>(empty)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [youtube, setYoutube] = useState('')
  const reload = () => getAdminNews().then(setItems).catch((e: Error) => setError(e.message))
  useEffect(() => { void reload() }, [])
  const edit = (item: NewsItem) => {
    setEditing(item.id)
    setForm({ title: item.title, summary: item.summary, body: item.body, category: item.category,
      media: item.media, published: item.published, featured: item.featured, publishAt: item.publishAt })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
  const submit = async (event: FormEvent) => {
    event.preventDefault(); setBusy(true); setError('')
    try { await saveNews(form, editing); setEditing(undefined); setForm(empty); await reload() }
    catch (e) { setError(e instanceof Error ? e.message : 'No se pudo guardar') }
    finally { setBusy(false) }
  }
  const upload = async (files: FileList | null) => {
    if (!files?.length) return
    setBusy(true); setError('')
    try {
      const uploaded = []
      for (const file of Array.from(files)) uploaded.push(await uploadNewsMedia(file))
      setForm((previous) => ({ ...previous, media: [...previous.media, ...uploaded] }))
    } catch (e) { setError(e instanceof Error ? e.message : 'No se pudo subir el archivo') }
    finally { setBusy(false) }
  }
  const addYoutube = () => {
    if (!/^https:\/\/(www\.)?(youtube\.com\/watch\?v=[A-Za-z0-9_-]{11}|youtu\.be\/[A-Za-z0-9_-]{11})$/.test(youtube)) {
      setError('Introduce un enlace válido de YouTube'); return
    }
    setForm((previous) => ({ ...previous, media: [...previous.media, { url: youtube, type: 'VIDEO' }] }))
    setYoutube(''); setError('')
  }
  return <section className="news-page news-admin">
    <Link to="/admin" className="text-link">← Administración</Link>
    <h1>Gestión de noticias</h1><p>Solo administradores pueden publicar noticias.</p>
    {error && <p role="alert" className="news-error">{error}</p>}
    <form className="news-form" onSubmit={submit}>
      <h2>{editing ? 'Editar noticia' : 'Nueva noticia'}</h2>
      <label>Título<input required maxLength={180} value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></label>
      <label>Resumen<textarea required maxLength={500} value={form.summary} onChange={(e) => setForm({ ...form, summary: e.target.value })} /></label>
      <label>Contenido<textarea required rows={8} maxLength={20000} value={form.body} onChange={(e) => setForm({ ...form, body: e.target.value })} /></label>
      <label>Categoría<select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value as NewsInput['category'] })}>
        {Object.entries(CATEGORY_LABELS).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
      </select></label>
      <label>Fotografías o vídeos (máximo 25 MB por archivo)<input type="file" accept="image/jpeg,image/png,image/webp,video/mp4,video/webm" multiple disabled={busy} onChange={(e) => { void upload(e.target.files); e.target.value = '' }} /></label>
      <div className="news-inline"><input placeholder="Enlace de YouTube" value={youtube} onChange={(e) => setYoutube(e.target.value)} />
        <button type="button" className="btn btn--ghost" onClick={addYoutube}>Añadir vídeo</button></div>
      <div className="news-attachments">{form.media.map((media, index) => <div key={index}>
        <span>{media.type === 'IMAGE' ? 'Foto' : 'Vídeo'} {index + 1}</span>
        <button type="button" onClick={() => setForm({ ...form, media: form.media.filter((_, i) => i !== index) })}>Quitar</button>
      </div>)}</div>
      <label className="news-check"><input type="checkbox" checked={form.featured} onChange={(e) => setForm({ ...form, featured: e.target.checked })} /> Destacar en inicio</label>
      <label className="news-check"><input type="checkbox" checked={form.published} onChange={(e) => setForm({ ...form, published: e.target.checked })} /> Publicada</label>
      <label>Programar publicación (opcional)<input type="datetime-local" value={form.publishAt ? form.publishAt.slice(0, 16) : ''} onChange={(e) => setForm({ ...form, publishAt: e.target.value ? new Date(e.target.value).toISOString() : null })} /></label>
      <div className="news-inline"><button className="btn btn--primary" disabled={busy} type="submit">{busy ? 'Procesando...' : 'Guardar noticia'}</button>
        {editing && <button className="btn btn--ghost" type="button" onClick={() => { setEditing(undefined); setForm(empty) }}>Cancelar edición</button>}</div>
    </form>
    <h2>Publicaciones</h2>
    <div className="news-admin-list">{items.map((item) => <article key={item.id}>
      <div><strong>{item.title}</strong><p>{item.published ? 'Publicada o programada' : 'Borrador'} · {CATEGORY_LABELS[item.category]}</p></div>
      <div className="news-inline"><button className="btn btn--ghost btn--sm" type="button" onClick={() => edit(item)}>Editar</button>
        <button className="btn btn--danger btn--sm" type="button" onClick={async () => {
          if (!window.confirm('¿Eliminar esta noticia?')) return
          try { await deleteNews(item.id); await reload() } catch (e) { setError(e instanceof Error ? e.message : 'Error al eliminar') }
        }}>Eliminar</button></div>
    </article>)}</div>
  </section>
}
