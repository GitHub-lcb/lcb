import type { MenuProps } from 'antd'
import { Layout, Menu, Avatar, Dropdown, message } from 'antd'
import {
  UserOutlined, BellOutlined, AppstoreOutlined,
  TeamOutlined, MenuUnfoldOutlined, BookOutlined,
  FileOutlined, SafetyOutlined, CodeOutlined
} from '@ant-design/icons'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { authApi } from '@/api/auth'
import { useStore } from '@/store'

const { Header, Sider, Content } = Layout

const menuItems: MenuProps['items'] = [
  { key: '/dashboard', icon: <AppstoreOutlined />, label: 'Dashboard' },
  { key: 'system', icon: <TeamOutlined />, label: '系统管理', children: [
    { key: '/system/user', icon: <UserOutlined />, label: '用户管理' },
    { key: '/system/role', icon: <TeamOutlined />, label: '角色管理' },
    { key: '/system/menu', icon: <MenuUnfoldOutlined />, label: '菜单管理' },
  ]},
  { key: '/dict', icon: <BookOutlined />, label: '字典管理' },
  { key: '/file', icon: <FileOutlined />, label: '文件管理' },
  { key: '/monitor/audit-log', icon: <SafetyOutlined />, label: '审计日志' },
  { key: '/generator', icon: <CodeOutlined />, label: '代码生成' },
]

export default function MainLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const [collapsed, setCollapsed] = useState(false)
  const fetchUserInfo = useStore((s) => s.fetchUserInfo)
  const permissions = useStore((s) => s.permissions)
  const logout = useStore((s) => s.logout)

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) {
      navigate('/login')
      return
    }
    if (permissions.length === 0) {
      fetchUserInfo().catch(() => {
        logout()
        navigate('/login')
      })
    }
  }, [])

  const handleLogout = () => {
    authApi.logout().then(() => {
      logout()
      navigate('/login')
    }).catch(() => {
      logout()
      navigate('/login')
    })
  }

  const dropdownItems: MenuProps['items'] = [
    { key: 'profile', label: '个人信息' },
    { type: 'divider' },
    { key: 'logout', label: '退出登录', danger: true, onClick: handleLogout },
  ]

  const pathParts = location.pathname.split('/').filter(Boolean)
  const selectedKeys = pathParts.length > 0 ? ['/' + pathParts.slice(0, 2).join('/')] : ['/dashboard']

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed}>
        <div style={{ height: 32, margin: 16, background: 'rgba(255,255,255,.2)', borderRadius: 6, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontWeight: 600 }}>
          {collapsed ? 'L' : 'LCB'}
        </div>
        <Menu theme="dark" mode="inline" selectedKeys={selectedKeys} defaultOpenKeys={['system']}
          items={menuItems}
          onClick={({ key }) => navigate(key)} />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: '0 24px', display: 'flex', alignItems: 'center', borderBottom: '1px solid #f0f0f0' }}>
          <span style={{ fontWeight: 600, fontSize: 16 }}>LCB 管理系统</span>
          <div style={{ flex: 1 }} />
          <BellOutlined style={{ fontSize: 18, marginRight: 16, cursor: 'pointer' }} />
          <Dropdown menu={{ items: dropdownItems }} placement="bottomRight">
            <Avatar icon={<UserOutlined />} style={{ cursor: 'pointer', background: '#1677ff' }} />
          </Dropdown>
        </Header>
        <Content style={{ margin: 24, minHeight: 280 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
