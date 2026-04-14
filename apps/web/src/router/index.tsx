import { createBrowserRouter } from 'react-router-dom'
import HomePage from '../pages/HomePage'
import RoutesPage from '../pages/RoutesPage'
import EventsPage from '../pages/EventsPage'
import MerchPage from '../pages/MerchPage'
import ProfilePage from '../pages/ProfilePage'
import NotFoundPage from '../pages/NotFoundPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <HomePage />,
    errorElement: <NotFoundPage />,
  },
  {
    path: '/routes',
    element: <RoutesPage />,
  },
  {
    path: '/events',
    element: <EventsPage />,
  },
  {
    path: '/merch',
    element: <MerchPage />,
  },
  {
    path: '/profile',
    element: <ProfilePage />,
  },
])