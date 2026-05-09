import { Table, Button, Space, Modal, Form, Input, message, Card, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import { roleApi } from '../../../api/system/role'
import type { SysRole } from '../../../types/api'

export default function RolePage() {
  const [data, setData] = useState<SysRole[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [modalOpen, setModalOpen] = useState(false)
  const [editingRow, setEditingRow] = useState<SysRole | null>(null)
  const [form] = Form.useForm()

  const columns = [
    { title: '角色名称', dataIndex: 'roleName', key: 'roleName' },
    { title: '权限标识', dataIndex: 'roleKey', key: 'roleKey' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: number) =>
      <Tag color={v === 1 ? 'green' : 'red'}>{v === 1 ? '正常' : '禁用'}</Tag>
    },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
    { title: '操作', key: 'action', render: (_: unknown, record: SysRole) => (
      <Space>
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
        <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>删除</Button>
      </Space>
    )},
  ]

  const fetchData = async () => {
    setLoading(true)
    const res = await roleApi.page({ page, pageSize: 10 })
    setData(res.records || [])
    setTotal(res.total || 0)
    setLoading(false)
  }

  useEffect(() => { fetchData() }, [page])

  const handleAdd = () => {
    setEditingRow(null)
    form.resetFields()
    setModalOpen(true)
  }

  const handleEdit = (row: SysRole) => {
    setEditingRow(row)
    form.setFieldsValue(row)
    setModalOpen(true)
  }

  const handleDelete = (id: number) => {
    Modal.confirm({ title: '确认删除', content: '确定要删除此角色吗？', onOk: async () => {
      await roleApi.remove(id)
      message.success('删除成功')
      fetchData()
    }})
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (editingRow) {
      await roleApi.edit({ ...values, id: editingRow.id })
    } else {
      await roleApi.add(values)
    }
    message.success(editingRow ? '修改成功' : '新增成功')
    setModalOpen(false)
    fetchData()
  }

  return (
    <Card title="角色管理" extra={<Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>新增角色</Button>}>
      <Table rowKey="id" columns={columns} dataSource={data} loading={loading}
        pagination={{ current: page, total, pageSize: 10, onChange: (p) => setPage(p) }} />
      <Modal title={editingRow ? '编辑角色' : '新增角色'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)}>
        <Form form={form} layout="vertical">
          <Form.Item name="id" hidden><Input /></Form.Item>
          <Form.Item label="角色名称" name="roleName" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item label="权限标识" name="roleKey" rules={[{ required: true }]}><Input /></Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}
