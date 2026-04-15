import { useNavigate } from 'react-router-dom'
import RouteList from '../components/RouteList/RouteList'
import { mockRoutes } from '../data/mockRoutes'

export default function RoutesPage() {
  const navigate = useNavigate()

  const handleCreateRoute = () => {
    navigate('/admin/routes')
  }

  return (
    <section>
      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
        <div>
          <h1>Rutas</h1>
          <p>Descubre las próximas rutas del club y apúntate.</p>
        </div>

        <button
          onClick={handleCreateRoute}
          style={{
            height: '40px',
            padding: '0 16px',
            background: 'orange',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
          }}
        >
          Crear ruta
        </button>
      </div>

      <RouteList routes={mockRoutes} />
    </section>
  )
}