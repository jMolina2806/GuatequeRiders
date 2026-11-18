import React from 'react'
import ReactDOM from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { AuthProvider } from '@/features/auth/context/AuthContext'
import { CartProvider } from '@/features/merch/context/CartContext'
import { MerchProvider } from '@/features/merch/context/MerchContext'
import { router } from './router'
import './styles/global.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AuthProvider>
      <MerchProvider>
        <CartProvider>
          <RouterProvider router={router} />
        </CartProvider>
      </MerchProvider>
    </AuthProvider>
  </React.StrictMode>,
)
