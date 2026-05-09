import { Table, Button, Space, Modal, Form, Input, InputNumber, Select, message, Card } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import { menuApi } from '@/api/system/menu'
import type { SysMenu } from '@/types/api'

export default function MenuPage() {
  const [data, setData] = useState<SysMenu[]>([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [editingRow, setEditingRow] = useState<SysMenu | null>(null)
  const [form] = Form.useForm()

  const columns = [
    { title: '菜单名称', dataIndex: 'menuName', key: 'menuName' },
    { title: '图标', dataIndex: 'icon', key: 'icon' },
    { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
    { title: '权限标识', dataIndex: 'permission', key: 'permission' },
    { title: '路由', dataIndex: 'path', key: 'path' },
    { title: '类型', dataIndex: 'menuType', key: 'menuType', render: (v: string) =>
      ({ M: '目录', C: '菜单', F: '按钮' })[v] || v
    },
    { title: '操作', key: 'action', render: (_: unknown, record: SysMenu) => (
      <Space>
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
        <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>删除</Button>
      </Space>
    )},
  ]

  const fetchData = async () => {
    setLoading(true)
    const res = await menuApi.tree()
    setData(res || [])
    setLoading(false)
  }

  useEffect(() => { fetchData() }, [])

  const handleAdd = (parent?: SysMenu) => {
    setEditingRow(null)
    form.resetFields()
    if (parent) form.setFieldValue('parentId', parent.id)
    setModalOpen(true)
  }

  const handleEdit = (row: SysMenu) => {
    setEditingRow(row)
    form.setFieldsValue(row)
    setModalOpen(true)
  }

  const handleDelete = (id: number) => {
    Modal.confirm({ title: '确认删除', content: '确定要删除此菜单吗？', onOk: async () => {
      await menuApi.remove(id)
      message.success('删除成功')
      fetchData()
    }})
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (editingRow) {
      await menuApi.edit({ ...values, id: editingRow.id })
    } else {
      await menuApi.add(values)
    }
    message.success(editingRow ? '修改成功' : '新增成功')
    setModalOpen(false)
    fetchData()
  }

  return (
    <Card title="菜单管理" extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd()}>新增菜单</Button>}>
      <Table rowKey="id" columns={columns} dataSource={data} loading={loading}
        pagination={false} />
      <Modal title={editingRow ? '编辑菜单' : '新增菜单'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={600}>
        <Form form={form} layout="vertical">
          <Form.Item name="id" hidden><Input /></Form.Item>
          <Form.Item label="菜单名称" name="menuName" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item label="父菜单ID" name="parentId"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item label="权限标识" name="permission"><Input /></Form.Item>
          <Form.Item label="路由地址" name="path"><Input /></Form.Item>
          <Form.Item label="图标" name="icon"><Input /></Form.Item>
          <Form.Item label="排序" name="sort"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item label="类型" name="menuType" initialValue="C">
            <Select options={[
              { value: 'M', label: '目录' },
              { value: 'C', label: '菜单' },
              { value: 'F', label: '按钮' },
            ]} />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}
