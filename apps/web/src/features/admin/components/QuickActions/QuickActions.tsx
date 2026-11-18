import { Link } from 'react-router-dom'
import './QuickActions.css'

type QuickAction = {
  to: string
  label: string
  description: string
}

const ACTIONS: QuickAction[] = [
  { to: '/routes', label: 'Gestionar rutas', description: 'Crear y editar rutas del club' },
  { to: '/events', label: 'Gestionar eventos', description: 'Crear y editar eventos del club' },
  { to: '/admin/users', label: 'Gestionar usuarios', description: 'Cambiar los roles de los usuarios' },
  { to: '/admin/members', label: 'Gestionar socios', description: 'Revisar pendientes del grupo general y administrar carnets activos' },
  { to: '/admin/orders', label: 'Gestionar campañas Merch', description: 'Abrir, cerrar y consultar campañas' },
]

export default function QuickActions() {
  return (
    <div className="quick-actions">
      {ACTIONS.map((action) => (
        <Link to={action.to} className="quick-actions__card" key={action.to}>
          <span className="quick-actions__label">{action.label}</span>
          <span className="quick-actions__description">{action.description}</span>
        </Link>
      ))}
    </div>
  )
}
