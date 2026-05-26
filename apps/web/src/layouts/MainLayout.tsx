import { Outlet } from 'react-router-dom'
import Navbar from '@/components/Navbar'
import './MainLayout.css'

export default function MainLayout() {
  return (
    <div>
      <Navbar />

      <main className="main-layout__content">
        <Outlet />
      </main>
    </div>
  )
}