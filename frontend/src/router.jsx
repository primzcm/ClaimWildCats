import { createBrowserRouter } from 'react-router-dom';
import App from './App';
import { HomePage } from './pages/HomePage';
import { SearchPage } from './pages/SearchPage';
import { ReportLostPage } from './pages/ReportLostPage';
import { ReportFoundPage } from './pages/ReportFoundPage';
import { LostPage } from './pages/LostPage';
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
import { GetStartedPage } from './pages/GetStartedPage';
import { ProtectedRoute, PublicOnlyRoute } from './components/ProtectedRoute';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    errorElement: <NotFoundPage />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'get-started', element: <GetStartedPage /> },
      { path: 'search', element: <SearchPage /> },
      { path: 'lost', element: <LostPage /> },

      { path: 'items/new/lost', element: <ProtectedRoute><ReportLostPage /></ProtectedRoute> },
      { path: 'items/new/found', element: <ProtectedRoute><ReportFoundPage /></ProtectedRoute> },
      { path: 'items/:id', element: <ItemDetailsPage /> },
      { path: 'items/:id/edit', element: <ProtectedRoute><EditItemPage /></ProtectedRoute> },
      { path: 'items/:id/claim', element: <ProtectedRoute><ClaimItemPage /></ProtectedRoute> },
      { path: 'me', element: <ProtectedRoute><ProfilePage /></ProtectedRoute> },
      { path: 'me/reports', element: <ProtectedRoute><MyReportsPage /></ProtectedRoute> },
      { path: 'settings', element: <ProtectedRoute><SettingsPage /></ProtectedRoute> },
      { path: 'auth/login', element: <PublicOnlyRoute><LoginPage /></PublicOnlyRoute> },
      { path: 'auth/register', element: <PublicOnlyRoute redirectTo='/me'><RegisterPage /></PublicOnlyRoute> },
      { path: 'admin', element: <ProtectedRoute><AdminDashboardPage /></ProtectedRoute> },
      { path: 'admin/users', element: <ProtectedRoute><AdminUsersPage /></ProtectedRoute> },
      { path: 'admin/reports', element: <ProtectedRoute><AdminReportsPage /></ProtectedRoute> },
      { path: 'admin/moderation', element: <ProtectedRoute><AdminModerationPage /></ProtectedRoute> },
      { path: 'admin/analytics', element: <ProtectedRoute><AdminAnalyticsPage /></ProtectedRoute> },
      { path: '*', element: <NotFoundPage /> }
    ],
  },
]);
