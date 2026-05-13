import { Form, Input, Button, Card, message } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { authApi } from '@/api/auth'
import { useStore } from '@/store'
import type { LoginParams } from '@/types/api'

export default function Login() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const fetchUserInfo = useStore((s) => s.fetchUserInfo)

  const onFinish = async (values: LoginParams) => {
    setLoading(true)
    try {
      const res = await authApi.login(values)
      localStorage.setItem('token', res.token)
      await fetchUserInfo()
      message.success('登录成功')
      navigate('/')
    } catch {
      // error handled by interceptor
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      display: 'flex', justifyContent: 'center', alignItems: 'center',
      minHeight: '100vh', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }}>
      <Card title="LCB 管理系统" style={{ width: 400, borderRadius: 8 }}>
        <Form onFinish={onFinish} size="large">
          <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>登 录</Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
