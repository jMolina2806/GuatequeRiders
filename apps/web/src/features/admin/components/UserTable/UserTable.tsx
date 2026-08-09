import type { User } from '@/features/auth/types/auth.types'
import RoleBadge from '../RoleBadge'
import './UserTable.css'

type UserTableProps = {
  users: User[]
  roleUpdatingId: number | null
  onToggleRole: (user: User) => void
}

function formatDate(iso: string): string {
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return iso
  return date.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

export default function UserTable({ users, roleUpdatingId, onToggleRole }: UserTableProps) {
  return (
    <div className="user-table__wrapper">
      <table className="user-table">
        <thead>
          <tr>
            <th>Usuario</th>
            <th>Email</th>
            <th>Rol</th>
            <th>Registro</th>
            <th className="user-table__actions-head">Acciones</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => {
            const isUpdating = roleUpdatingId === user.id
            const nextRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
            return (
              <tr key={user.id}>
                <td className="user-table__username">{user.username}</td>
                <td>{user.email}</td>
                <td>
                  <RoleBadge role={user.role} />
                </td>
                <td>{formatDate(user.createdAt)}</td>
                <td className="user-table__actions">
                  <button
                    className="user-table__role-btn"
                    onClick={() => onToggleRole(user)}
                    disabled={isUpdating}
                    type="button"
                  >
                    {isUpdating ? 'Cambiando...' : `Hacer ${nextRole === 'ADMIN' ? 'admin' : 'usuario'}`}
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
