import type { RouteObject } from 'react-router-dom'
import MainLayout from '../layouts/MainLayout'
import Login from '../pages/login'
import Dashboard from '../pages/dashboard'
import UserPage from '../pages/system/user'
import RolePage from '../pages/system/role'
import MenuPage from '../pages/system/menu'
import DictPage from '../pages/dict'
import FilePage from '../pages/file'
import AuditLog from '../pages/monitor/audit-log'
import GeneratorPage from '../pages/generator'

export const routes: RouteObject[] = [
  { path: '/login', element: <Login /> },
  {
    path: '/',
    element: <MainLayout />,
    children: [
      { index: true, element: <Dashboard /> },
      { path: 'system/user', element: <UserPage /> },
      { path: 'system/role', element: <RolePage /> },
      { path: 'system/menu', element: <MenuPage /> },
      { path: 'dict', element: <DictPage /> },
      { path: 'file', element: <FilePage /> },
      { path: 'monitor/audit-log', element: <AuditLog /> },
      { path: 'generator', element: <GeneratorPage /> },
    ],
  },
]
