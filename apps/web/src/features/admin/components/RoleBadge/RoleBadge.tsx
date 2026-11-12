import type { UserRole } from '@/features/auth/types/auth.types'
import './RoleBadge.css'

export default function RoleBadge({ role }: { role: UserRole }) {
  const label = role === 'COLLABORATOR' ? 'COLABORADOR' : role
  return <span className={`role-badge role-badge--${role.toLowerCase()}`}>{label}</span>
}
