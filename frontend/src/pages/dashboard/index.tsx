import { Row, Col, Card, Statistic, Table, Tag } from 'antd'
import { ArrowUpOutlined, UserOutlined, ShoppingCartOutlined, DollarOutlined } from '@ant-design/icons'

const recentOrders = [
  { key: '1', orderNo: 'ORD-2026-0001', user: '张三', amount: 1280, status: '已完成' },
  { key: '2', orderNo: 'ORD-2026-0002', user: '李四', amount: 560, status: '处理中' },
  { key: '3', orderNo: 'ORD-2026-0003', user: '王五', amount: 3200, status: '待审核' },
]

const columns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo' },
  { title: '用户', dataIndex: 'user', key: 'user' },
  { title: '金额', dataIndex: 'amount', key: 'amount', render: (v: number) => `¥${v.toLocaleString()}` },
  { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => {
    const colors: Record<string, string> = { '已完成': 'green', '处理中': 'blue', '待审核': 'gold' }
    return <Tag color={colors[v] || 'default'}>{v}</Tag>
  }},
]

export default function Dashboard() {
  return (
    <>
      <Row gutter={[16, 16]}>
        <Col span={8}>
          <Card hoverable>
            <Statistic title="总用户数" value={1234} prefix={<UserOutlined />}
              suffix={<small style={{ color: '#52c41a' }}><ArrowUpOutlined /> 12%</small>} />
          </Card>
        </Col>
        <Col span={8}>
          <Card hoverable>
            <Statistic title="订单量" value={456} prefix={<ShoppingCartOutlined />}
              suffix={<small style={{ color: '#1890ff' }}><ArrowUpOutlined /> 5%</small>} />
          </Card>
        </Col>
        <Col span={8}>
          <Card hoverable>
            <Statistic title="收入" value={78920} prefix={<DollarOutlined />} precision={2}
              suffix={<small style={{ color: '#fa8c16' }}><ArrowUpOutlined /> 8%</small>} />
          </Card>
        </Col>
      </Row>
      <Card title="最近订单" style={{ marginTop: 24 }}>
        <Table columns={columns} dataSource={recentOrders} pagination={false} />
      </Card>
    </>
  )
}
