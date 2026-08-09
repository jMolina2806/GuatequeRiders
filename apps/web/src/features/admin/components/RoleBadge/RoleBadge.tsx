import type { UserRole } from '@/features/auth/types/auth.types'
import './RoleBadge.css'

export default function RoleBadge({ role }: { role: UserRole }) {
  return <span className={`role-badge role-badge--${role.toLowerCase()}`}>{role}</span>
}
