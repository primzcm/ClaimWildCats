import { createBrowserRouter } from 'react-router-dom';
import App from './App';
import { HomePage } from './pages/HomePage';
import { SearchPage } from './pages/SearchPage';
import { ReportLostPage } from './pages/ReportLostPage';
import { ReportFoundPage } from './pages/ReportFoundPage';
import { ItemDetailsPage } from './pages/ItemDetailsPage';
import { EditItemPage } from './pages/EditItemPage';
import { ClaimItemPage } from './pages/ClaimItemPage';
import { ProfilePage } from './pages/ProfilePage';
import { MyReportsPage } from './pages/MyReportsPage';
import { SettingsPage } from './pages/SettingsPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { AdminDashboardPage } from './pages/AdminDashboardPage';
import { AdminUsersPage } from './pages/AdminUsersPage';
import { AdminReportsPage } from './pages/AdminReportsPage';
import { AdminModerationPage } from './pages/AdminModerationPage';
import { AdminAnalyticsPage } from './pages/AdminAnalyticsPage';
import { NotFoundPage } from './pages/NotFoundPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    errorElement: <NotFoundPage />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'search', element: <SearchPage /> },
      { path: 'items/new/lost', element: <ReportLostPage /> },
      { path: 'items/new/found', element: <ReportFoundPage /> },
      { path: 'items/:id', element: <ItemDetailsPage /> },
      { path: 'items/:id/edit', element: <EditItemPage /> },
      { path: 'items/:id/claim', element: <ClaimItemPage /> },
      { path: 'me', element: <ProfilePage /> },
      { path: 'me/reports', element: <MyReportsPage /> },
      { path: 'settings', element: <SettingsPage /> },
      { path: 'auth/login', element: <LoginPage /> },
      { path: 'auth/register', element: <RegisterPage /> },
      { path: 'admin', element: <AdminDashboardPage /> },
      { path: 'admin/users', element: <AdminUsersPage /> },
      { path: 'admin/reports', element: <AdminReportsPage /> },
      { path: 'admin/moderation', element: <AdminModerationPage /> },
      { path: 'admin/analytics', element: <AdminAnalyticsPage /> },
      { path: '*', element: <NotFoundPage /> }
    ],
  },
]);
