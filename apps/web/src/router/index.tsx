import { createBrowserRouter } from 'react-router-dom'
import MainLayout from '@/layouts/MainLayout'
import HomePage from '@/pages/HomePage'
import RoutesPage from '@/features/routes/pages/RoutesPage'
import EventsPage from '@/features/events/pages/EventsPage'
import CircuitsPage from '@/features/circuits/pages/CircuitsPage'
import MerchPage from '@/pages/MerchPage'
import CollaboratorsPage from '@/features/collaborators/pages/CollaboratorsPage'
import ProfilePage from '@/features/profile/pages/ProfilePage'
import UnsubscribePage from '@/features/profile/pages/UnsubscribePage'
import LoginPage from '@/features/auth/pages/LoginPage'
import RegisterPage from '@/features/auth/pages/RegisterPage'
import NotFoundPage from '@/pages/NotFoundPage'
import CheckoutPage from '@/pages/CheckoutPage'
import OrderPendingPage from '@/pages/OrderPendingPage'
import CheckoutPaymentPage from '@/pages/CheckoutPaymentPage'
import CheckoutResultPage from '@/pages/CheckoutResultPage'
import PreparationSheetPage from '@/pages/PreparationSheetPage'
import LegalInfoPage from '@/pages/LegalInfoPage'
import AdminDashboardPage from '@/features/admin/pages/AdminDashboardPage'
import AdminUsersPage from '@/features/admin/pages/AdminUsersPage'
import AdminOrdersPage from '@/features/admin/pages/AdminOrdersPage'
import RequireAdmin from '@/features/admin/components/RequireAdmin'

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
        path: 'circuits',
        element: <CircuitsPage />,
      },
      {
        path: 'merch',
        element: <MerchPage />,
      },
      {
        path: 'collaborators',
        element: <CollaboratorsPage />,
      },
      {
        path: 'profile',
        element: <ProfilePage />,
      },
      {
        path: 'unsubscribe',
        element: <UnsubscribePage />,
      },
      {
        path: 'legal',
        element: <LegalInfoPage />,
      },
      {
        path: 'privacy',
        element: <LegalInfoPage />,
      },
      {
        path: 'merch-terms',
        element: <LegalInfoPage />,
      },
      {
        path: 'checkout',
        element: <CheckoutPage />,
      },
      {
        path: 'checkout/pending/:id',
        element: <OrderPendingPage />,
      },
      {
        path: 'checkout/payment/:id',
        element: <CheckoutPaymentPage />,
      },
      {
        path: 'checkout/success/:id',
        element: <CheckoutResultPage result="success" />,
      },
      {
        path: 'checkout/cancel/:id',
        element: <CheckoutResultPage result="cancel" />,
      },
      {
        path: 'admin',
        element: (
          <RequireAdmin>
            <AdminDashboardPage />
          </RequireAdmin>
        ),
      },
      {
        path: 'admin/users',
        element: (
          <RequireAdmin>
            <AdminUsersPage />
          </RequireAdmin>
        ),
      },
      {
        path: 'admin/orders',
        element: (
          <RequireAdmin>
            <AdminOrdersPage />
          </RequireAdmin>
        ),
      },
      {
        path: 'admin/orders/preparation-sheet/:campaignId',
        element: (
          <RequireAdmin>
            <PreparationSheetPage />
          </RequireAdmin>
        ),
      },
    ],
  },
])
