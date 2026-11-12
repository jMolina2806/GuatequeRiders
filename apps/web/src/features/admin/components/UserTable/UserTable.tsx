import type { User, UserRole } from '@/features/auth/types/auth.types'
import RoleBadge from '../RoleBadge'
import './UserTable.css'

type UserTableProps = {
  users: User[]
  roleUpdatingId: number | null
  membershipUpdatingId: number | null
  onChangeRole: (user: User, role: UserRole) => void
  onToggleMembership: (user: User) => void
}

function formatDate(iso: string): string {
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return iso
  return date.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

export default function UserTable({
  users,
  roleUpdatingId,
  membershipUpdatingId,
  onChangeRole,
  onToggleMembership,
}: UserTableProps) {
  return (
    <div className="user-table__wrapper">
      <table className="user-table">
        <thead>
          <tr>
            <th scope="col">Usuario</th>
            <th scope="col">Email</th>
            <th scope="col">Teléfono</th>
            <th scope="col">Rol</th>
            <th scope="col">Estado</th>
            <th scope="col">Registro</th>
            <th scope="col">Acciones</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => {
            const isUpdating = roleUpdatingId === user.id
            const isMembershipUpdating = membershipUpdatingId === user.id

            return (
              <tr key={user.id}>
                <td className="user-table__username" data-label="Usuario">
                  {user.username}
                </td>
                <td data-label="Email">{user.email}</td>
                <td data-label="Teléfono">{user.phone ?? '—'}</td>
                <td data-label="Rol">
                  <RoleBadge role={user.role} />
                </td>
                <td data-label="Estado">
                  <span className={user.memberActive ? 'user-table__membership user-table__membership--active' : 'user-table__membership'}>
                    {user.role === 'COLLABORATOR' ? 'Verificación de carnets' : user.memberActive ? 'Socio activo' : 'Miembro registrado'}
                  </span>
                </td>
                <td data-label="Registro">{formatDate(user.createdAt)}</td>
                <td className="user-table__actions" data-label="Acciones">
                  <button
                    className="btn btn--ghost btn--sm"
                    onClick={() => onToggleMembership(user)}
                    disabled={isMembershipUpdating || user.role !== 'USER'}
                    title={user.role !== 'USER' ? 'Solo se puede cambiar el estado de socio a cuentas de usuario' : 'Solo tras comprobar WhatsApp'}
                    type="button"
                  >
                    {isMembershipUpdating ? 'Actualizando...' : user.memberActive ? 'Quitar socio' : 'Hacer socio'}
                  </button>
                  {user.role !== 'ADMIN' && (
                    <button className="btn btn--ghost btn--sm" onClick={() => onChangeRole(user, 'ADMIN')} disabled={isUpdating} type="button">
                      {isUpdating ? 'Cambiando...' : 'Hacer admin'}
                    </button>
                  )}
                  {user.role !== 'COLLABORATOR' && (
                    <button className="btn btn--ghost btn--sm" onClick={() => onChangeRole(user, 'COLLABORATOR')} disabled={isUpdating} type="button">
                      {isUpdating ? 'Cambiando...' : 'Hacer colaborador'}
                    </button>
                  )}
                  {user.role !== 'USER' && (
                    <button className="btn btn--ghost btn--sm" onClick={() => onChangeRole(user, 'USER')} disabled={isUpdating} type="button">
                      {isUpdating ? 'Cambiando...' : 'Hacer usuario'}
                    </button>
                  )}
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
