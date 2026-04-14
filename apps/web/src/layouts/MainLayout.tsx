import { NavLink, Outlet } from 'react-router-dom'

const navItems = [
  { to: '/', label: 'Inicio', end: true },
  { to: '/routes', label: 'Rutas' },
  { to: '/events', label: 'Eventos' },
  { to: '/merch', label: 'Merchandising' },
  { to: '/profile', label: 'Perfil' },
]

export default function MainLayout() {
  return (
    <div style={styles.appShell}>
      <header style={styles.header}>
        <div style={styles.brand}>Guateque Riders</div>

        <nav style={styles.nav}>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              style={({ isActive }) => ({
                ...styles.link,
                ...(isActive ? styles.linkActive : {}),
              })}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </header>

      <main style={styles.main}>
        <Outlet />
      </main>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  appShell: {
    minHeight: '100vh',
    backgroundColor: '#0f1115',
    color: '#f5f7fa',
    fontFamily: 'Arial, sans-serif',
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '16px 24px',
    borderBottom: '1px solid #22262e',
    position: 'sticky',
    top: 0,
    backgroundColor: '#0f1115',
  },
  brand: {
    fontSize: '20px',
    fontWeight: 700,
  },
  nav: {
    display: 'flex',
    gap: '12px',
    flexWrap: 'wrap',
  },
  link: {
    textDecoration: 'none',
    color: '#c7ced8',
    padding: '8px 12px',
    borderRadius: '8px',
    transition: '0.2s ease',
  },
  linkActive: {
    backgroundColor: '#ff7a1a',
    color: '#ffffff',
  },
  main: {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '24px',
  },
}