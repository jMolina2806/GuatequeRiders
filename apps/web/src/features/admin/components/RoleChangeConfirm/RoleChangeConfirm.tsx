import Modal from '@/components/Modal'
import RoleBadge from '../RoleBadge'
import type { User, UserRole } from '@/features/auth/types/auth.types'
import './RoleChangeConfirm.css'

type RoleChangeConfirmProps = {
  user: User | null
  targetRole: UserRole | null
  isUpdating: boolean
  onConfirm: () => void
  onCancel: () => void
}

export default function RoleChangeConfirm({ user, targetRole, isUpdating, onConfirm, onCancel }: RoleChangeConfirmProps) {
  if (!user || !targetRole) return null

  const hint = targetRole === 'ADMIN'
    ? 'Tendrá acceso a todas las funciones de administración y un carnet de socio.'
    : targetRole === 'COLLABORATOR'
      ? 'Solo podrá verificar carnets. Comprueba que la cuenta pertenece al establecimiento. Si era socio, se desactivará su carnet.'
      : 'Perderá los permisos especiales. Su estado de socio deberá comprobarse de nuevo si procede.'

  return (
    <Modal isOpen={user !== null} onClose={onCancel} title="Cambiar rol">
      <div className="role-change-confirm">
        <p className="role-change-confirm__text">
          ¿Cambiar el rol de <strong>{user.username}</strong> de <RoleBadge role={user.role} /> a{' '}
          <RoleBadge role={targetRole} />?
        </p>
        <p className="role-change-confirm__hint">{hint}</p>
        <div className="role-change-confirm__actions">
          <button className="btn btn--ghost" onClick={onCancel} disabled={isUpdating} type="button">Cancelar</button>
          <button className="btn btn--primary" onClick={onConfirm} disabled={isUpdating} type="button">
            {isUpdating ? 'Cambiando...' : 'Confirmar'}
          </button>
        </div>
      </div>
    </Modal>
  )
}
