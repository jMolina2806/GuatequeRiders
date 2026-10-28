import CircuitCard from '../CircuitCard'
import type { Circuit } from '@/features/circuits/types/circuit.types'

type CircuitListProps = {
  circuits: Circuit[]
  isAdmin?: boolean
  onEdit?: (circuit: Circuit) => void
  onDelete?: (circuitId: number) => void
  onPublish?: (circuitId: number) => void
  onComplete?: (circuitId: number) => void
  onCancel?: (circuitId: number) => void
  onDetails?: (circuit: Circuit) => void
}

export default function CircuitList({
  circuits,
  isAdmin = false,
  onEdit,
  onDelete,
  onPublish,
  onComplete,
  onCancel,
  onDetails,
}: CircuitListProps) {
  return (
    <section className="circuit-list">
      {circuits.map((circuit) => (
        <CircuitCard
          key={circuit.id}
          circuit={circuit}
          isAdmin={isAdmin}
          onEdit={onEdit}
          onDelete={onDelete}
          onPublish={onPublish}
          onComplete={onComplete}
          onCancel={onCancel}
          onDetails={onDetails}
        />
      ))}
    </section>
  )
}
