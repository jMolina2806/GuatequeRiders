import { createBrowserRouter } from 'react-router-dom'
import MainLayout from '@/layouts/MainLayout'
import HomePage from '@/pages/HomePage'
import RoutesPage from '@/features/routes/pages/RoutesPage'
import EventsPage from '@/pages/EventsPage'
import MerchPage from '@/pages/MerchPage'
import ProfilePage from '@/pages/ProfilePage'
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