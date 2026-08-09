import { createBrowserRouter } from 'react-router-dom'
import MainLayout from '@/layouts/MainLayout'
import HomePage from '@/pages/HomePage'
import RoutesPage from '@/features/routes/pages/RoutesPage'
import EventsPage from '@/features/events/pages/EventsPage'
import MerchPage from '@/pages/MerchPage'
import ProfilePage from '@/features/profile/pages/ProfilePage'
import LoginPage from '@/features/auth/pages/LoginPage'
import RegisterPage from '@/features/auth/pages/RegisterPage'
import NotFoundPage from '@/pages/NotFoundPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <MainLayout />,
    errorElement: <NotFoundPage />,
    children: [
      {
        index: true,
        element: <HomePage />,
      },
      {
        path: 'routes',
        element: <RoutesPage />,
      },
      {
        path: 'login',
        element: <LoginPage />,
      },
      {
        path: 'register',
        element: <RegisterPage />,
      },
      {
        path: 'events',
        element: <EventsPage />,
      },
      {
        path: 'merch',
        element: <MerchPage />,
      },
      {
        path: 'profile',
        element: <ProfilePage />,
      },
    ],
  },
])