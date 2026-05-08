import { Form, Input, Button, Card, message } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../../api/auth'

export default function Login() {
  const navigate = useNavigate()

  const onFinish = async (values: any) => {
    try {
      const res: any = await authApi.login(values)
      localStorage.setItem('token', res.token)
      message.success('登录成功')
      navigate('/')
    } catch {
      // error handled by interceptor
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
            <Button type="primary" htmlType="submit" block>登 录</Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
