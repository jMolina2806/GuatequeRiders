import { useMemo, useState } from 'react'
import Modal from '@/components/Modal'
import { useAuth } from '@/features/auth/context/useAuth'
import CollaboratorCard from '../components/CollaboratorCard'
import CollaboratorForm from '../components/CollaboratorForm'
import { useCollaborators } from '../hooks/useCollaborators'
import type { Collaborator, CollaboratorType, CreateCollaboratorRequest } from '../types/collaborator.types'
import { COLLABORATOR_TYPE_LABELS } from '../types/collaborator.types'
import './CollaboratorsPage.css'

type CollaboratorFilter = 'ALL' | CollaboratorType

const FILTERS: CollaboratorFilter[] = [
  'ALL',
  'WORKSHOP',
  'SHOP',
  'BRAND',
  'EQUIPMENT',
  'PHOTOGRAPHY',
  'CIRCUIT',
  'OTHER',
]

export default function CollaboratorsPage() {
  const { isAdmin, isMember } = useAuth()
  const {
    collaborators,
    editingCollaborator,
    isLoading,
    error,
    feedback,
    retry,
    saveCollaborator,
    deleteCollaborator,
    setCollaboratorActive,
    removeCollaboratorImage,
    editCollaborator,
    cancelEdit,
    clearFeedback,
  } = useCollaborators()
  const [activeFilter, setActiveFilter] = useState<CollaboratorFilter>('ALL')
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [pendingDelete, setPendingDelete] = useState<Collaborator | null>(null)

  const filteredCollaborators = useMemo(
    () => activeFilter === 'ALL'
      ? collaborators
      : collaborators.filter((collaborator) => collaborator.type === activeFilter),
    [activeFilter, collaborators],
  )

  const openCreate = () => {
    cancelEdit()
    setIsModalOpen(true)
  }

  const openEdit = (collaborator: Collaborator) => {
    editCollaborator(collaborator)
    setIsModalOpen(true)
  }

  const closeForm = () => {
    cancelEdit()
    setIsModalOpen(false)
  }

  const handleSave = async (data: CreateCollaboratorRequest, imageFile: File | null) => {
    const saved = await saveCollaborator(data, imageFile)
    if (saved) setIsModalOpen(false)
    return saved
  }

  const handleDelete = async () => {
    if (!pendingDelete) return
    const deleted = await deleteCollaborator(pendingDelete.id)
    if (deleted) setPendingDelete(null)
  }

  return (
    <section className="collaborators-page">
      <header className="collaborators-page__header">
        <div>
          <span className="eyebrow">Red Guateque</span>
          <h1>Colaboradores</h1>
          <p>
            Talleres, tiendas, circuitos y personas que ayudan a que cada kilómetro cuente.
          </p>
        </div>
        <div className="collaborators-page__header-side">
          <div className="collaborators-page__count" aria-label={`${collaborators.length} colaboradores`}>
            <strong>{String(collaborators.length).padStart(2, '0')}</strong>
            <span>{isAdmin ? 'registros visibles' : 'colaboradores activos'}</span>
          </div>
          {isAdmin && (
            <button className="btn btn--primary" onClick={openCreate} type="button">
              + Añadir colaborador
            </button>
          )}
        </div>
      </header>

      {feedback && (
        <div className={`collaborators-page__feedback collaborators-page__feedback--${feedback.type}`} role="status">
          <span>{feedback.message}</span>
          <button onClick={clearFeedback} type="button" aria-label="Cerrar">✕</button>
        </div>
      )}

      {error && (
        <div className="collaborators-page__error" role="alert">
          <span>{error}</span>
          <button className="btn btn--outline-danger btn--sm" onClick={retry} type="button">
            Reintentar
          </button>
        </div>
      )}

      <div className="collaborators-page__filters" aria-label="Filtrar colaboradores">
        {FILTERS.map((filter) => (
          <button
            key={filter}
            className={activeFilter === filter ? 'is-active' : ''}
            onClick={() => setActiveFilter(filter)}
            type="button"
          >
            {filter === 'ALL' ? 'Todos' : COLLABORATOR_TYPE_LABELS[filter]}
          </button>
        ))}
      </div>

      {isLoading ? (
        <div className="collaborators-page__state" role="status">Cargando colaboradores...</div>
      ) : filteredCollaborators.length > 0 ? (
        <div className="collaborators-page__grid">
          {filteredCollaborators.map((collaborator) => (
            <CollaboratorCard
              key={collaborator.id}
              collaborator={collaborator}
              isAdmin={isAdmin}
              canViewBenefits={isAdmin || isMember}
              onEdit={openEdit}
              onDelete={setPendingDelete}
              onToggleActive={(item) => setCollaboratorActive(item, !item.active)}
            />
          ))}
        </div>
      ) : (
        <div className="collaborators-page__empty">
          <span aria-hidden="true">+</span>
          <h2>{isAdmin ? 'Sin registros en este filtro' : 'Próxima incorporación'}</h2>
          <p>{isAdmin ? 'Añade un colaborador o cambia el filtro.' : 'Estamos preparando nuevas colaboraciones en esta categoría.'}</p>
        </div>
      )}

      <Modal
        isOpen={isModalOpen}
        onClose={closeForm}
        title={editingCollaborator ? 'Editar colaborador' : 'Añadir colaborador'}
      >
        <CollaboratorForm
          initialValues={editingCollaborator}
          onSubmit={handleSave}
          onRemoveImage={removeCollaboratorImage}
          onCancel={closeForm}
        />
      </Modal>

      <Modal
        isOpen={pendingDelete !== null}
        onClose={() => setPendingDelete(null)}
        title="Eliminar colaborador"
      >
        <div className="collaborators-page__confirm">
          <p>
            ¿Quieres eliminar <strong>{pendingDelete?.name}</strong>? También se eliminará su imagen gestionada.
          </p>
          <div>
            <button className="btn btn--danger" onClick={handleDelete} type="button">
              Eliminar
            </button>
            <button className="btn btn--ghost" onClick={() => setPendingDelete(null)} type="button">
              Cancelar
            </button>
          </div>
        </div>
      </Modal>
    </section>
  )
}
