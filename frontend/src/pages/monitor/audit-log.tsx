import { Table, Card, Tag } from 'antd'
import { useState, useEffect } from 'react'
import { auditLogApi } from '../../api/monitor/audit-log'

interface AuditLogItem {
  id: number
  username: string
  operation: string
  method: string
  duration: number
  status: number
  createTime: string
}

export default function AuditLog() {
  const [data, setData] = useState<AuditLogItem[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)

  const columns = [
    { title: '操作用户', dataIndex: 'username', key: 'username' },
    { title: '操作', dataIndex: 'operation', key: 'operation' },
    { title: '方法', dataIndex: 'method', key: 'method', ellipsis: true },
    { title: '耗时(ms)', dataIndex: 'duration', key: 'duration' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: number) =>
      <Tag color={v === 1 ? 'green' : 'red'}>{v === 1 ? '成功' : '失败'}</Tag>
    },
    { title: '操作时间', dataIndex: 'createTime', key: 'createTime' },
  ]

  useEffect(() => {
    setLoading(true)
    auditLogApi.page({ page, pageSize: 10 }).then((res) => {
      setData(res.records || [])
      setTotal(res.total || 0)
    }).finally(() => setLoading(false))
  }, [page])

  return (
    <Card title="审计日志">
      <Table rowKey="id" columns={columns} dataSource={data} loading={loading}
        pagination={{ current: page, total, pageSize: 10, onChange: (p) => setPage(p) }} />
    </Card>
  )
}
