import './StatusBadge.css'

export type StatusBadgeTone = 'neutral' | 'info' | 'success' | 'warning' | 'danger' | 'accent'

type StatusBadgeProps = {
  status: string
  label?: string
  tone?: StatusBadgeTone
}

const DEFAULT_PRESENTATION: Record<string, { label: string; tone: StatusBadgeTone }> = {
  DRAFT: { label: 'Borrador', tone: 'neutral' },
  PUBLISHED: { label: 'Publicado', tone: 'success' },
  IN_PROGRESS: { label: 'En curso', tone: 'info' },
  COMPLETED: { label: 'Completado', tone: 'accent' },
  CLOSED: { label: 'Cerrado', tone: 'accent' },
  CANCELLED: { label: 'Cancelado', tone: 'danger' },
}

export default function StatusBadge({ status, label, tone }: StatusBadgeProps) {
  const presentation = DEFAULT_PRESENTATION[status]
  const visibleLabel = label ?? presentation?.label ?? status
  const visibleTone = tone ?? presentation?.tone ?? 'neutral'

  return (
    <span className={`status-badge status-badge--${visibleTone}`}>
      {visibleLabel}
    </span>
  )
}
