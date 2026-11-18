import { useEffect, useRef, useState, type ChangeEvent } from 'react'
import Modal from '@/components/Modal'
import { deleteRoutePhoto, getRoutePhotos, uploadRoutePhoto } from '../../services/routes.service'
import type { Route, RoutePhoto } from '../../types/route.types'
import './RoutePhotoGallery.css'

type RoutePhotoGalleryProps = {
  route: Route
  isAdmin: boolean
  isAuthenticated: boolean
  isRegistered: boolean
  currentUserId?: number
}

type UploadItem = {
  id: string
  file: File
  previewUrl: string
  status: 'queued' | 'uploading' | 'success' | 'error'
  error?: string
}

type UploadProgress = {
  completed: number
  total: number
}

const MAX_FILE_SIZE = 5 * 1024 * 1024
const ACCEPTED_TYPES = new Set(['image/png', 'image/jpeg', 'image/webp'])

function formatFileSize(size: number): string {
  if (size < 1024 * 1024) return `${Math.max(1, Math.round(size / 1024))} KB`
  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}

function getUploadError(file: File): string | undefined {
  if (!ACCEPTED_TYPES.has(file.type)) return 'Formato no permitido'
  if (file.size > MAX_FILE_SIZE) return 'Supera el límite de 5 MB'
  return undefined
}

export default function RoutePhotoGallery({
  route,
  isAdmin,
  isAuthenticated,
  isRegistered,
  currentUserId,
}: RoutePhotoGalleryProps) {
  const [photos, setPhotos] = useState<RoutePhoto[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState<UploadItem[]>([])
  const [isUploading, setIsUploading] = useState(false)
  const [progress, setProgress] = useState<UploadProgress | null>(null)
  const [selectedPhoto, setSelectedPhoto] = useState<RoutePhoto | null>(null)
  const [deletingId, setDeletingId] = useState<number | null>(null)
  const pendingRef = useRef<UploadItem[]>([])

  const canUpload =
    isAdmin ||
    (isAuthenticated &&
      isRegistered &&
      (route.status === 'IN_PROGRESS' || route.status === 'COMPLETED'))

  useEffect(() => {
    pendingRef.current = pending
  }, [pending])

  useEffect(() => {
    return () => {
      pendingRef.current.forEach((item) => URL.revokeObjectURL(item.previewUrl))
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    setIsLoading(true)
    setError(null)
    setSelectedPhoto(null)
    setProgress(null)
    setPending((current) => {
      current.forEach((item) => URL.revokeObjectURL(item.previewUrl))
      return []
    })

    getRoutePhotos(route.id)
      .then((data) => {
        if (!cancelled) setPhotos(data)
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Error al cargar la galería')
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [route.id])

  const refreshPhotos = async () => {
    try {
      setPhotos(await getRoutePhotos(route.id))
      setError(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al actualizar la galería')
    }
  }

  const handleFileSelection = (event: ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files ?? [])
    if (files.length === 0) return

    const nextItems = files.map<UploadItem>((file) => ({
      id: crypto.randomUUID(),
      file,
      previewUrl: URL.createObjectURL(file),
      status: getUploadError(file) ? 'error' : 'queued',
      error: getUploadError(file),
    }))
    setPending((current) => [...current, ...nextItems])
    setProgress(null)
    event.target.value = ''
  }

  const removePending = (itemId: string) => {
    setPending((current) => {
      const item = current.find((candidate) => candidate.id === itemId)
      if (item) URL.revokeObjectURL(item.previewUrl)
      return current.filter((candidate) => candidate.id !== itemId)
    })
  }

  const updatePending = (itemId: string, update: Partial<UploadItem>) => {
    setPending((current) =>
      current.map((item) => (item.id === itemId ? { ...item, ...update } : item)),
    )
  }

  const handleUpload = async () => {
    const queue = pending.filter((item) => item.status === 'queued')
    if (queue.length === 0 || isUploading) return

    let completed = pending.filter((item) => item.status !== 'queued').length
    setProgress({ completed, total: pending.length })
    setIsUploading(true)

    for (const item of queue) {
      updatePending(item.id, { status: 'uploading', error: undefined })
      try {
        await uploadRoutePhoto(route.id, item.file)
        updatePending(item.id, { status: 'success' })
      } catch (err) {
        updatePending(item.id, {
          status: 'error',
          error: err instanceof Error ? err.message : 'No se pudo subir la imagen',
        })
      }
      completed += 1
      setProgress({ completed, total: pending.length })
    }

    setIsUploading(false)
    await refreshPhotos()
  }

  const handleDelete = async (photo: RoutePhoto) => {
    if (!window.confirm('¿Eliminar esta foto de la ruta?')) return
    setDeletingId(photo.id)
    try {
      await deleteRoutePhoto(route.id, photo.id)
      setPhotos((current) => current.filter((candidate) => candidate.id !== photo.id))
      if (selectedPhoto?.id === photo.id) setSelectedPhoto(null)
      setError(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo eliminar la foto')
    } finally {
      setDeletingId(null)
    }
  }

  const canDelete = (photo: RoutePhoto) => isAdmin || photo.uploadedBy.id === currentUserId
  const queuedCount = pending.filter((item) => item.status === 'queued').length

  return (
    <section className="route-photo-gallery" aria-labelledby="route-photo-gallery-title">
      <div className="route-photo-gallery__header">
        <div>
          <span className="route-photo-gallery__eyebrow">Memoria de carretera</span>
          <h3 className="route-photo-gallery__title" id="route-photo-gallery-title">
            Galería de ruta
          </h3>
        </div>
        <span className="route-photo-gallery__count">{photos.length} fotos</span>
      </div>

      {canUpload && (
        <div className="route-photo-gallery__uploader">
          <div className="route-photo-gallery__uploader-copy">
            <strong>Añade tus mejores momentos</strong>
            <span>PNG, JPEG o WebP · máximo 5 MB por foto</span>
          </div>
          <label className="btn btn--ghost route-photo-gallery__select">
            Seleccionar fotos
            <input
              type="file"
              accept="image/png,image/jpeg,image/webp"
              multiple
              onChange={handleFileSelection}
              disabled={isUploading}
              aria-label="Seleccionar fotos para la ruta"
            />
          </label>
        </div>
      )}

      {pending.length > 0 && canUpload && (
        <div className="route-photo-gallery__pending">
          <div className="route-photo-gallery__pending-header">
            <span>Fotos seleccionadas</span>
            {progress && (
              <span aria-live="polite">
                {progress.completed} / {progress.total}
              </span>
            )}
          </div>
          <ul className="route-photo-gallery__pending-list">
            {pending.map((item) => (
              <li className="route-photo-gallery__pending-item" key={item.id}>
                <img src={item.previewUrl} alt="" />
                <div className="route-photo-gallery__pending-info">
                  <strong title={item.file.name}>{item.file.name}</strong>
                  <span>{formatFileSize(item.file.size)}</span>
                  {item.status === 'queued' && <small>Lista</small>}
                  {item.status === 'uploading' && <small>Subiendo...</small>}
                  {item.status === 'success' && (
                    <small className="route-photo-gallery__status--success">Subida</small>
                  )}
                  {item.status === 'error' && (
                    <small className="route-photo-gallery__status--error">{item.error}</small>
                  )}
                </div>
                <button
                  className="route-photo-gallery__remove"
                  type="button"
                  onClick={() => removePending(item.id)}
                  disabled={isUploading}
                  aria-label={`Quitar ${item.file.name}`}
                >
                  Quitar
                </button>
              </li>
            ))}
          </ul>
          <button
            className="btn btn--primary route-photo-gallery__upload"
            type="button"
            onClick={handleUpload}
            disabled={isUploading || queuedCount === 0}
          >
            {isUploading ? 'Subiendo fotos...' : `Subir ${queuedCount} foto${queuedCount === 1 ? '' : 's'}`}
          </button>
        </div>
      )}

      {error && <p className="route-photo-gallery__error" role="alert">{error}</p>}

      {isLoading ? (
        <div className="route-photo-gallery__loading" role="status">Cargando fotos...</div>
      ) : photos.length === 0 ? (
        <div className="route-photo-gallery__empty">
          <span className="route-photo-gallery__empty-mark">+</span>
          <strong>Aún no hay fotos</strong>
          <span>La galería se encenderá cuando llegue el primer recuerdo.</span>
        </div>
      ) : (
        <div className="route-photo-gallery__grid">
          {photos.map((photo) => (
            <figure className="route-photo-gallery__photo" key={photo.id}>
              <button
                className="route-photo-gallery__thumbnail"
                type="button"
                onClick={() => setSelectedPhoto(photo)}
                aria-label={`Ver foto subida por ${photo.uploadedBy.username}`}
              >
                <img
                  src={photo.imageUrl}
                  alt={`Foto de ${route.title} subida por ${photo.uploadedBy.username}`}
                  loading="lazy"
                />
                <span>Ver</span>
              </button>
              <figcaption className="route-photo-gallery__caption">
                <span>Por {photo.uploadedBy.username}</span>
                <div className="route-photo-gallery__actions">
                  <a href={photo.downloadUrl} download>
                    Descargar
                  </a>
                  {canDelete(photo) && (
                    <button
                      type="button"
                      onClick={() => handleDelete(photo)}
                      disabled={deletingId === photo.id}
                    >
                      {deletingId === photo.id ? '...' : 'Eliminar'}
                    </button>
                  )}
                </div>
              </figcaption>
            </figure>
          ))}
        </div>
      )}

      <Modal
        isOpen={selectedPhoto !== null}
        onClose={() => setSelectedPhoto(null)}
        title={selectedPhoto ? `Foto de ${selectedPhoto.uploadedBy.username}` : 'Foto'}
      >
        {selectedPhoto && (
          <div className="route-photo-gallery__lightbox">
            <img
              src={selectedPhoto.imageUrl}
              alt={`Foto ampliada de ${route.title} subida por ${selectedPhoto.uploadedBy.username}`}
            />
            <div className="route-photo-gallery__lightbox-actions">
              <a className="btn btn--primary" href={selectedPhoto.downloadUrl} download>
                Descargar
              </a>
              {canDelete(selectedPhoto) && (
                <button
                  className="btn btn--outline-danger"
                  type="button"
                  onClick={() => handleDelete(selectedPhoto)}
                  disabled={deletingId === selectedPhoto.id}
                >
                  {deletingId === selectedPhoto.id ? 'Eliminando...' : 'Eliminar'}
                </button>
              )}
            </div>
          </div>
        )}
      </Modal>
    </section>
  )
}
