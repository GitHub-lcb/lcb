import { Table, Button, Space, message, Card, Select, Modal } from 'antd'
import { CodeOutlined, DownloadOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import request from '../../api/request'

export default function GeneratorPage() {
  const [tables, setTables] = useState([])
  const [loading, setLoading] = useState(false)
  const [selectedTable, setSelectedTable] = useState<number | null>(null)

  const dbColumns = [
    { title: '列名', dataIndex: 'column_name', key: 'column_name' },
    { title: '注释', dataIndex: 'column_comment', key: 'column_comment' },
    { title: '类型', dataIndex: 'data_type', key: 'data_type' },
  ]

  const fetchTables = async () => {
    setLoading(true)
    const res: any = await request.get('/generator/table/page', { params: { page: 1, pageSize: 100 } })
    setTables(res.records || [])
    setLoading(false)
  }

  useEffect(() => { fetchTables() }, [])

  const handleGenerate = async () => {
    if (!selectedTable) {
      message.warning('请先选择一张表')
      return
    }
    Modal.confirm({
      title: '确认生成',
      content: '将直接写入项目对应目录，确认生成？',
      onOk: async () => {
        await request.post(`/generator/code/${selectedTable}`)
        message.success('代码已生成到对应目录')
      }
    })
  }

  return (
    <Card title="代码生成器" extra={
      <Space>
        <Select placeholder="选择已导入的表" style={{ width: 300 }}
          value={selectedTable}
          onChange={setSelectedTable}
          options={tables.map((t: any) => ({ value: t.id, label: `${t.tableName} (${t.tableComment || '无注释'})` }))} />
        <Button type="primary" icon={<CodeOutlined />} onClick={handleGenerate}>生成代码</Button>
      </Space>
    }>
      <Table rowKey="id" columns={[
        { title: '表名', dataIndex: 'tableName', key: 'tableName' },
        { title: '注释', dataIndex: 'tableComment', key: 'tableComment' },
        { title: '类名', dataIndex: 'className', key: 'className' },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
      ]} dataSource={tables} loading={loading} pagination={false} />
    </Card>
  )
}
