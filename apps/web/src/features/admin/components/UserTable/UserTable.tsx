import type { User } from '@/features/auth/types/auth.types'
import RoleBadge from '../RoleBadge'
import './UserTable.css'

type UserTableProps = {
  users: User[]
  roleUpdatingId: number | null
  membershipUpdatingId: number | null
  onToggleRole: (user: User) => void
  onToggleMembership: (user: User) => void
}

function formatDate(iso: string): string {
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return iso
  return date.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function formatMembershipDate(value: string | null): string {
  if (!value) return '—'
  return new Date(`${value}T12:00:00`).toLocaleDateString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

export default function UserTable({
  users,
  roleUpdatingId,
  membershipUpdatingId,
  onToggleRole,
  onToggleMembership,
}: UserTableProps) {
  return (
    <div className="user-table__wrapper">
      <table className="user-table">
        <thead>
          <tr>
            <th scope="col">Usuario</th>
            <th scope="col">Email</th>
            <th scope="col">Rol</th>
            <th scope="col">Socio</th>
            <th scope="col">N.º socio</th>
            <th scope="col">Validez</th>
            <th scope="col">Registro</th>
            <th scope="col">Acciones</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => {
            const isRoleUpdating = roleUpdatingId === user.id
            const isMembershipUpdating = membershipUpdatingId === user.id
            const nextRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
            return (
              <tr key={user.id}>
                <td className="user-table__username" data-label="Usuario">
                  {user.username}
                </td>
                <td data-label="Email">{user.email}</td>
                <td data-label="Rol">
                  <RoleBadge role={user.role} />
                </td>
                <td data-label="Socio">{user.memberActive ? 'Activo' : 'No'}</td>
                <td data-label="N.º socio">{user.memberNumber ?? '—'}</td>
                <td data-label="Validez">{formatMembershipDate(user.membershipValidUntil)}</td>
                <td data-label="Registro">{formatDate(user.createdAt)}</td>
                <td className="user-table__actions" data-label="Acciones">
                  <button
                    className="btn btn--ghost btn--sm"
                    onClick={() => onToggleRole(user)}
                    disabled={isRoleUpdating || isMembershipUpdating}
                    type="button"
                  >
                    {isRoleUpdating ? 'Cambiando...' : `Hacer ${nextRole === 'ADMIN' ? 'admin' : 'usuario'}`}
                  </button>
                  <button
                    className="btn btn--ghost btn--sm"
                    onClick={() => onToggleMembership(user)}
                    disabled={isRoleUpdating || isMembershipUpdating}
                    type="button"
                  >
                    {isMembershipUpdating
                      ? 'Actualizando...'
                      : user.memberActive
                        ? 'Desactivar socio'
                        : 'Activar socio'}
                  </button>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
