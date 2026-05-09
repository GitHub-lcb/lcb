import { Table, Button, Space, Modal, Form, Input, message, Card, Tag } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import { dictApi } from '../../api/system/dict'
import type { SysDictType } from '../../types/api'

export default function DictPage() {
  const [data, setData] = useState<SysDictType[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [modalOpen, setModalOpen] = useState(false)
  const [form] = Form.useForm()

  const columns = [
    { title: '字典名称', dataIndex: 'dictName', key: 'dictName' },
    { title: '字典类型', dataIndex: 'dictType', key: 'dictType' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: number) =>
      <Tag color={v === 1 ? 'green' : 'red'}>{v === 1 ? '正常' : '禁用'}</Tag>
    },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  ]

  const fetchData = async () => {
    setLoading(true)
    const res = await dictApi.typePage({ page, pageSize: 10 })
    setData(res.records || [])
    setTotal(res.total || 0)
    setLoading(false)
  }

  useEffect(() => { fetchData() }, [page])

  const handleSubmit = async () => {
    const values = await form.validateFields()
    await dictApi.addType(values)
    message.success('新增成功')
    setModalOpen(false)
    fetchData()
  }

  return (
    <Card title="字典管理" extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true) }}>新增字典</Button>}>
      <Table rowKey="id" columns={columns} dataSource={data} loading={loading}
        pagination={{ current: page, total, pageSize: 10, onChange: (p) => setPage(p) }} />
      <Modal title="新增字典类型" open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)}>
        <Form form={form} layout="vertical">
          <Form.Item label="字典名称" name="dictName" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item label="字典类型" name="dictType" rules={[{ required: true }]}><Input /></Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}
