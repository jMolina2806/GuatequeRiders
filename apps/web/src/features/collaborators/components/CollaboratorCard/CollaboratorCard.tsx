import StatusBadge from '@/components/StatusBadge'
import type { Collaborator } from '../../types/collaborator.types'
import {
  COLLABORATOR_TYPE_FALLBACKS,
  COLLABORATOR_TYPE_LABELS,
  getBenefitLabel,
  getDiscountLabel,
} from '../../types/collaborator.types'
import './CollaboratorCard.css'

type CollaboratorCardProps = {
  collaborator: Collaborator
  isAdmin?: boolean
  onEdit?: (collaborator: Collaborator) => void
  onDelete?: (collaborator: Collaborator) => void
  onToggleActive?: (collaborator: Collaborator) => void
}

export default function CollaboratorCard({
  collaborator,
  isAdmin = false,
  onEdit,
  onDelete,
  onToggleActive,
}: CollaboratorCardProps) {
  const discountLabel = getDiscountLabel(collaborator)

  return (
    <article className={`collaborator-card ${!collaborator.active ? 'collaborator-card--inactive' : ''}`}>
      <div className="collaborator-card__header">
        <div className="collaborator-card__mark">
          {collaborator.imageUrl ? (
            <img src={collaborator.imageUrl} alt={`Logo de ${collaborator.name}`} />
          ) : (
            <span aria-hidden="true">{COLLABORATOR_TYPE_FALLBACKS[collaborator.type]}</span>
          )}
        </div>
        <div className="collaborator-card__badges">
          <span className="collaborator-card__type">
            {COLLABORATOR_TYPE_LABELS[collaborator.type]}
          </span>
          {collaborator.featured && <span className="collaborator-card__featured">Home</span>}
          {isAdmin && (
            <StatusBadge
              status={collaborator.active ? 'ACTIVE' : 'INACTIVE'}
              label={collaborator.active ? 'Activo' : 'Inactivo'}
              tone={collaborator.active ? 'success' : 'neutral'}
            />
          )}
        </div>
      </div>

      <h2 className="collaborator-card__title">{collaborator.name}</h2>
      <p className="collaborator-card__description">{collaborator.shortDescription}</p>

      <div className="collaborator-card__benefit">
        <span>Ventaja para miembros</span>
        <strong>{getBenefitLabel(collaborator)}</strong>
        {discountLabel && collaborator.benefit && <small>{collaborator.benefit}</small>}
      </div>

      <div className="collaborator-card__footer">
        {collaborator.websiteUrl && (
          <a
            className="btn btn--ghost btn--sm collaborator-card__link"
            href={collaborator.websiteUrl}
            target="_blank"
            rel="noopener noreferrer"
          >
            Visitar web <span aria-hidden="true">↗</span>
          </a>
        )}

        {isAdmin && (
          <div className="collaborator-card__admin-actions">
            <button className="btn btn--secondary btn--sm" onClick={() => onEdit?.(collaborator)} type="button">
              Editar
            </button>
            <button className="btn btn--ghost btn--sm" onClick={() => onToggleActive?.(collaborator)} type="button">
              {collaborator.active ? 'Desactivar' : 'Activar'}
            </button>
            <button className="btn btn--outline-danger btn--sm" onClick={() => onDelete?.(collaborator)} type="button">
              Eliminar
            </button>
          </div>
        )}
      </div>
    </article>
  )
}
