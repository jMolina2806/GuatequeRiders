import { useState } from 'react'
import Modal from '@/components/Modal'
import { useAuth } from '@/features/auth/context/useAuth'
import CircuitList from '../components/CircuitList'
import CircuitDetailsModal from '../components/CircuitDetailsModal'
import CircuitForm from '../components/CircuitForm'
import { useCircuits } from '../hooks/useCircuits'
import type { Circuit } from '../types/circuit.types'
import './CircuitsPage.css'

export default function CircuitsPage() {
  const {
    circuits,
    editingInitialValues,
    submitLabel,
    isLoading,
    error,
    feedback,
    retry,
    submitCircuit,
    editCircuit,
    deleteCircuit,
    publishCircuit,
    completeCircuit,
    cancelCircuit,
    clearFeedback,
    cancelEdit,
  } = useCircuits()
  const { isAdmin } = useAuth()

  const [isModalOpen, setIsModalOpen] = useState(false)
  const [detailsCircuit, setDetailsCircuit] = useState<Circuit | null>(null)

  const handleEdit = (circuit: Circuit) => {
    editCircuit(circuit)
    setDetailsCircuit(null)
    setIsModalOpen(true)
  }

  const handleCloseModal = () => {
    cancelEdit()
    setIsModalOpen(false)
  }

  const handleDelete = async (circuitId: number) => {
    await deleteCircuit(circuitId)
    setDetailsCircuit(null)
  }

  const handlePublish = async (circuitId: number) => {
    await publishCircuit(circuitId)
    setDetailsCircuit(null)
  }

  const handleComplete = async (circuitId: number) => {
    await completeCircuit(circuitId)
    setDetailsCircuit(null)
  }

  const handleCancel = async (circuitId: number) => {
    await cancelCircuit(circuitId)
    setDetailsCircuit(null)
  }

  return (
    <section className="circuits-page">
      <header className="circuits-page__header feature-page-header feature-page-header--circuits">
        <div className="circuits-page__copy feature-page-header__copy">
          <div className="circuits-page__eyebrow feature-page-header__eyebrow">
          <span>Guateque Riders / Motorsport</span>
          <span>{isAdmin ? 'Vista administrativa' : 'Actividades publicadas'}</span>
          </div>
          <h1 className="circuits-page__title feature-page-header__title">Circuitos</h1>
          <p className="circuits-page__subtitle feature-page-header__subtitle">
            Rodadas, tandas y experiencias de circuito para llevar la pasión por la moto un paso más allá.
          </p>
          <div className="circuits-page__notice feature-page-header__notice" role="note">
            <span className="circuits-page__notice-dot" aria-hidden="true" />
            <span>La inscripción se completa en la web externa de cada actividad.</span>
          </div>
        </div>
        {isAdmin && (
          <button className="btn btn--primary circuits-page__create feature-page-header__action" onClick={() => setIsModalOpen(true)} type="button">
            + Crear actividad
          </button>
        )}
      </header>

      {feedback && (
        <div className={`circuits-page__feedback circuits-page__feedback--${feedback.type}`} role="status">
          <span>{feedback.message}</span>
          <button className="circuits-page__feedback-close" onClick={clearFeedback} type="button" aria-label="Cerrar">
            ✕
          </button>
        </div>
      )}

      {error && (
        <div className="circuits-page__error" role="alert">
          <span>{error}</span>
          <button className="btn btn--outline-danger btn--sm" onClick={retry} type="button">
            Reintentar
          </button>
        </div>
      )}

      {isLoading ? (
        <div className="circuits-page__loading" role="status" aria-live="polite">
          <div className="circuits-page__spinner" />
          <p>Cargando circuitos...</p>
        </div>
      ) : circuits.length === 0 && !error ? (
        <div className="circuits-page__empty">
          <p>{isAdmin ? 'No hay actividades de circuito creadas.' : 'No hay circuitos publicados todavía.'}</p>
        </div>
      ) : (
        <CircuitList
          circuits={circuits}
          isAdmin={isAdmin}
          onDetails={setDetailsCircuit}
          onEdit={handleEdit}
          onDelete={handleDelete}
          onPublish={handlePublish}
          onComplete={handleComplete}
          onCancel={handleCancel}
        />
      )}

      <CircuitDetailsModal
        circuit={detailsCircuit}
        isAdmin={isAdmin}
        onClose={() => setDetailsCircuit(null)}
        onEdit={handleEdit}
        onDelete={handleDelete}
        onPublish={handlePublish}
        onComplete={handleComplete}
        onCancel={handleCancel}
      />

      <Modal isOpen={isModalOpen} onClose={handleCloseModal} title={submitLabel}>
        <CircuitForm
          onSubmit={submitCircuit}
          onCancel={handleCloseModal}
          initialValues={editingInitialValues}
          submitLabel={submitLabel}
        />
      </Modal>
    </section>
  )
}
