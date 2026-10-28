import { useEffect, useState } from 'react'
import type {
  Collaborator,
  CollaboratorType,
  CreateCollaboratorRequest,
} from '../../types/collaborator.types'
import { COLLABORATOR_TYPE_LABELS } from '../../types/collaborator.types'
import './CollaboratorForm.css'

const MAX_IMAGE_SIZE = 5 * 1024 * 1024
const ACCEPTED_IMAGE_TYPES = ['image/png', 'image/jpeg', 'image/webp']
const COLLABORATOR_TYPES = Object.keys(COLLABORATOR_TYPE_LABELS) as CollaboratorType[]

type CollaboratorFormProps = {
  initialValues?: Collaborator | null
  onSubmit: (data: CreateCollaboratorRequest, imageFile: File | null) => Promise<boolean>
  onRemoveImage?: (id: number) => Promise<boolean>
  onCancel?: () => void
}

type FormState = {
  name: string
  type: CollaboratorType
  shortDescription: string
  discountPercentage: string
  benefit: string
  websiteUrl: string
  featured: boolean
  active: boolean
}

const emptyForm: FormState = {
  name: '',
  type: 'WORKSHOP',
  shortDescription: '',
  discountPercentage: '',
  benefit: '',
  websiteUrl: '',
  featured: false,
  active: true,
}

export default function CollaboratorForm({
  initialValues = null,
  onSubmit,
  onRemoveImage,
  onCancel,
}: CollaboratorFormProps) {
  const [formData, setFormData] = useState<FormState>(() => toFormState(initialValues))
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [previewUrl, setPreviewUrl] = useState<string | null>(initialValues?.imageUrl ?? null)
  const [validationError, setValidationError] = useState<string | null>(null)
  const [isRemovingImage, setIsRemovingImage] = useState(false)

  useEffect(() => {
    setFormData(toFormState(initialValues))
    setImageFile(null)
    setPreviewUrl(initialValues?.imageUrl ?? null)
    setValidationError(null)
  }, [initialValues])

  useEffect(() => {
    if (!imageFile) return
    const objectUrl = URL.createObjectURL(imageFile)
    setPreviewUrl(objectUrl)
    return () => URL.revokeObjectURL(objectUrl)
  }, [imageFile])

  const handleChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value, type } = event.target
    const checked = type === 'checkbox' && 'checked' in event.target
      ? event.target.checked
      : undefined
    setFormData((previous) => ({
      ...previous,
      [name]: checked ?? value,
    }))
  }

  const handleImageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0] ?? null
    if (!file) return
    if (!ACCEPTED_IMAGE_TYPES.includes(file.type)) {
      setValidationError('La imagen debe ser PNG, JPEG o WebP.')
      event.target.value = ''
      return
    }
    if (file.size > MAX_IMAGE_SIZE) {
      setValidationError('La imagen no puede superar los 5 MB.')
      event.target.value = ''
      return
    }
    setValidationError(null)
    setImageFile(file)
  }

  const handleRemoveImage = async () => {
    if (!initialValues?.imageUrl || !onRemoveImage) return
    setIsRemovingImage(true)
    const removed = await onRemoveImage(initialValues.id)
    if (removed) {
      setImageFile(null)
      setPreviewUrl(null)
    }
    setIsRemovingImage(false)
  }

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const discount = formData.discountPercentage === '' ? null : Number(formData.discountPercentage)
    if (!formData.name.trim() || !formData.shortDescription.trim()) {
      setValidationError('Nombre y descripción breve son obligatorios.')
      return
    }
    if (discount !== null && (!Number.isInteger(discount) || discount < 0 || discount > 100)) {
      setValidationError('El descuento debe ser un número entero entre 0 y 100.')
      return
    }
    if (formData.websiteUrl.trim()) {
      try {
        const website = new URL(formData.websiteUrl.trim())
        if (website.protocol !== 'https:') throw new Error('invalid')
      } catch {
        setValidationError('La web debe ser una URL HTTPS válida.')
        return
      }
    }

    setValidationError(null)
    const saved = await onSubmit({
      name: formData.name.trim(),
      type: formData.type,
      shortDescription: formData.shortDescription.trim(),
      discountPercentage: discount,
      benefit: formData.benefit.trim() || null,
      websiteUrl: formData.websiteUrl.trim() || null,
      featured: formData.featured,
      active: formData.active,
    }, imageFile)
    if (saved && !initialValues) {
      setFormData(emptyForm)
      setImageFile(null)
      setPreviewUrl(null)
    }
  }

  return (
    <form className="collaborator-form" onSubmit={handleSubmit}>
      <div className="collaborator-form__grid">
        <div className="collaborator-form__field">
          <label htmlFor="collaborator-name">Nombre</label>
          <input
            id="collaborator-name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            maxLength={255}
          />
        </div>

        <div className="collaborator-form__field">
          <label htmlFor="collaborator-type">Tipo</label>
          <select id="collaborator-type" name="type" value={formData.type} onChange={handleChange}>
            {COLLABORATOR_TYPES.map((type) => (
              <option key={type} value={type}>{COLLABORATOR_TYPE_LABELS[type]}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="collaborator-form__field">
        <label htmlFor="collaborator-description">Descripción breve</label>
        <textarea
          id="collaborator-description"
          name="shortDescription"
          value={formData.shortDescription}
          onChange={handleChange}
          rows={3}
          maxLength={500}
          required
        />
      </div>

      <div className="collaborator-form__grid">
        <div className="collaborator-form__field">
          <label htmlFor="collaborator-discount">Porcentaje de descuento</label>
          <input
            id="collaborator-discount"
            name="discountPercentage"
            type="number"
            min="0"
            max="100"
            step="1"
            value={formData.discountPercentage}
            onChange={handleChange}
            placeholder="Opcional"
          />
        </div>

        <div className="collaborator-form__field">
          <label htmlFor="collaborator-website">Web</label>
          <input
            id="collaborator-website"
            name="websiteUrl"
            type="url"
            value={formData.websiteUrl}
            onChange={handleChange}
            placeholder="https://..."
          />
        </div>
      </div>

      <div className="collaborator-form__field">
        <label htmlFor="collaborator-benefit">Beneficio</label>
        <textarea
          id="collaborator-benefit"
          name="benefit"
          value={formData.benefit}
          onChange={handleChange}
          rows={2}
          maxLength={1000}
          placeholder="Ventaja pendiente de confirmar"
        />
      </div>

      <div className="collaborator-form__upload">
        <div className="collaborator-form__upload-heading">
          <div>
            <label htmlFor="collaborator-image">Imagen / Logo del colaborador</label>
            <span>PNG, JPEG o WebP · máximo 5 MB</span>
          </div>
          {previewUrl && initialValues?.imageUrl && !imageFile && onRemoveImage && (
            <button
              className="btn btn--outline-danger btn--sm"
              type="button"
              onClick={handleRemoveImage}
              disabled={isRemovingImage}
            >
              {isRemovingImage ? 'Eliminando...' : 'Quitar imagen'}
            </button>
          )}
        </div>
        <input
          id="collaborator-image"
          type="file"
          accept="image/png,image/jpeg,image/webp"
          onChange={handleImageChange}
        />
        {previewUrl ? (
          <div className="collaborator-form__preview">
            <img src={previewUrl} alt="Vista previa del colaborador" />
          </div>
        ) : (
          <div className="collaborator-form__preview collaborator-form__preview--empty">
            <span aria-hidden="true">+</span>
            <span>Sin imagen seleccionada</span>
          </div>
        )}
      </div>

      <div className="collaborator-form__toggles">
        <label>
          <input type="checkbox" name="featured" checked={formData.featured} onChange={handleChange} />
          <span>Mostrar destacado en Home</span>
        </label>
        <label>
          <input type="checkbox" name="active" checked={formData.active} onChange={handleChange} />
          <span>Colaborador activo</span>
        </label>
      </div>

      {validationError && <p className="collaborator-form__error" role="alert">{validationError}</p>}

      <div className="collaborator-form__actions">
        <button className="btn btn--primary" type="submit">
          {initialValues ? 'Guardar cambios' : 'Añadir colaborador'}
        </button>
        {onCancel && (
          <button className="btn btn--ghost" onClick={onCancel} type="button">
            Cancelar
          </button>
        )}
      </div>
    </form>
  )
}

function toFormState(collaborator: Collaborator | null | undefined): FormState {
  if (!collaborator) return emptyForm
  return {
    name: collaborator.name,
    type: collaborator.type,
    shortDescription: collaborator.shortDescription,
    discountPercentage: collaborator.discountPercentage?.toString() ?? '',
    benefit: collaborator.benefit ?? '',
    websiteUrl: collaborator.websiteUrl ?? '',
    featured: collaborator.featured,
    active: collaborator.active,
  }
}
