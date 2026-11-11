import { useEffect } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import Navbar from '@/components/Navbar'
import Footer from '@/components/Footer'
import './MainLayout.css'

const PAGE_TITLES: Record<string, string> = {
  '/': 'Inicio',
  '/routes': 'Rutas',
  '/events': 'Eventos',
  '/circuits': 'Circuitos',
  '/collaborators': 'Colaboradores',
  '/merch': 'Merch',
  '/profile': 'Perfil',
  '/checkout': 'Checkout',
  '/admin/orders': 'Campañas Merch',
  '/admin/orders/preparation-sheet': 'Hoja definitiva',
  '/admin': 'Administración',
  '/admin/users': 'Administración',
  '/login': 'Iniciar sesión',
  '/register': 'Registro',
  '/legal': 'Aviso legal',
  '/privacy': 'Privacidad',
  '/merch-terms': 'Condiciones Merch',
  '/cookies': 'Cookies y almacenamiento',
  '/member-card': 'Carnet de socio',
}

function PageTitle() {
  const { pathname } = useLocation()

  useEffect(() => {
    const normalizedPath = pathname.replace(/\/+$/, '') || '/'
    const pageTitle = PAGE_TITLES[normalizedPath]
      ?? (normalizedPath.startsWith('/checkout/pending')
        ? 'Pago pendiente'
        : normalizedPath.startsWith('/admin/orders/preparation-sheet/')
          ? 'Hoja definitiva'
          : undefined)
    document.title = pageTitle ? `${pageTitle} | Guateque Riders` : 'Guateque Riders'
  }, [pathname])

  return null
}

export default function MainLayout() {
  return (
    <div className="main-layout">
      <PageTitle />
      <Navbar />

      <main className="main-layout__content">
        <Outlet />
      </main>

      <Footer />
    </div>
  )
}
