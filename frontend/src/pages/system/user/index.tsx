import { Table, Button, Space, Modal, Form, Input, Select, message, Card, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, KeyOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import { userApi } from '../../../api/system/user'
import type { SysUser } from '../../../types/api'

export default function UserPage() {
  const [data, setData] = useState<SysUser[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [modalOpen, setModalOpen] = useState(false)
  const [resetModalOpen, setResetModalOpen] = useState(false)
  const [resetUserId, setResetUserId] = useState<number | null>(null)
  const [editingRow, setEditingRow] = useState<SysUser | null>(null)
  const [form] = Form.useForm()
  const [resetForm] = Form.useForm()

  const columns = [
    { title: '用户名', dataIndex: 'username', key: 'username' },
    { title: '昵称', dataIndex: 'nickname', key: 'nickname' },
    { title: '邮箱', dataIndex: 'email', key: 'email' },
    { title: '手机号', dataIndex: 'phone', key: 'phone' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: number) =>
      <Tag color={v === 1 ? 'green' : 'red'}>{v === 1 ? '正常' : '禁用'}</Tag>
    },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
    { title: '操作', key: 'action', render: (_: unknown, record: SysUser) => (
      <Space>
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
        <Button type="link" icon={<KeyOutlined />} onClick={() => handleResetPassword(record)}>重置密码</Button>
        <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>删除</Button>
      </Space>
    )},
  ]

  const fetchData = async () => {
    setLoading(true)
    const res = await userApi.page({ page, pageSize: 10 })
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

  const handleEdit = (row: SysUser) => {
    setEditingRow(row)
    form.setFieldsValue(row)
    setModalOpen(true)
  }

  const handleDelete = (id: number) => {
    Modal.confirm({ title: '确认删除', content: '确定要删除此用户吗？', onOk: async () => {
      await userApi.remove(id)
      message.success('删除成功')
      fetchData()
    }})
  }

  const handleResetPassword = (row: SysUser) => {
    setResetUserId(row.id)
    resetForm.resetFields()
    setResetModalOpen(true)
  }

  const handleResetSubmit = async () => {
    const values = await resetForm.validateFields()
    if (resetUserId !== null) {
      await userApi.resetPassword(resetUserId, values.password as string)
      message.success('密码重置成功')
      setResetModalOpen(false)
      setResetUserId(null)
    }
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (editingRow) {
      await userApi.edit({ ...values, id: editingRow.id })
    } else {
      await userApi.add(values)
    }
    message.success(editingRow ? '修改成功' : '新增成功')
    setModalOpen(false)
    fetchData()
  }

  return (
    <Card title="用户管理" extra={<Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>新增用户</Button>}>
      <Table rowKey="id" columns={columns} dataSource={data} loading={loading}
        pagination={{ current: page, total, pageSize: 10, onChange: (p) => setPage(p) }} />
      <Modal title={editingRow ? '编辑用户' : '新增用户'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={600}>
        <Form form={form} layout="vertical">
          <Form.Item name="id" hidden><Input /></Form.Item>
          <Form.Item label="用户名" name="username" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          {!editingRow && (
            <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}>
              <Input.Password />
            </Form.Item>
          )}
          <Form.Item label="昵称" name="nickname"><Input /></Form.Item>
          <Form.Item label="邮箱" name="email"><Input /></Form.Item>
          <Form.Item label="手机号" name="phone"><Input /></Form.Item>
          <Form.Item label="状态" name="status" initialValue={1}>
            <Select options={[{ value: 1, label: '正常' }, { value: 0, label: '禁用' }]} />
          </Form.Item>
        </Form>
      </Modal>
      <Modal title="重置密码" open={resetModalOpen} onOk={handleResetSubmit} onCancel={() => setResetModalOpen(false)} width={400}>
        <Form form={resetForm} layout="vertical">
          <Form.Item label="新密码" name="password" rules={[
            { required: true, message: '请输入新密码' },
            { min: 6, message: '密码至少6位' },
          ]}>
            <Input.Password />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}
