import { Table, Button, Space, Modal, message, Card, Upload } from 'antd'
import { UploadOutlined, DeleteOutlined, FileOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import request from '../../api/request'

export default function FilePage() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)

  const columns = [
    { title: '文件名', dataIndex: 'originalName', key: 'originalName' },
    { title: '大小', dataIndex: 'fileSize', key: 'fileSize', render: (v: number) =>
      v ? (v / 1024).toFixed(1) + ' KB' : '-'
    },
    { title: '类型', dataIndex: 'fileType', key: 'fileType' },
    { title: '上传时间', dataIndex: 'createTime', key: 'createTime' },
    { title: '操作', key: 'action', render: (_: any, record: any) => (
      <Space>
        <Button type="link" icon={<FileOutlined />} href={record.url} target="_blank">预览</Button>
        <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>删除</Button>
      </Space>
    )},
  ]

  const fetchData = async () => {
    setLoading(true)
    const res: any = await request.get('/file/page', { params: { page, pageSize: 10 } })
    setData(res.records || [])
    setTotal(res.total || 0)
    setLoading(false)
  }

  useEffect(() => { fetchData() }, [page])

  const handleDelete = (id: number) => {
    Modal.confirm({ title: '确认删除', content: '确定要删除此文件吗？', onOk: async () => {
      await request.delete(`/file/${id}`)
      message.success('删除成功')
      fetchData()
    }})
  }

  const handleUpload = async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    await request.post('/file/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    message.success('上传成功')
    fetchData()
    return false
  }

  return (
    <Card title="文件管理" extra={
      <Upload beforeUpload={handleUpload} showUploadList={false} accept="*">
        <Button type="primary" icon={<UploadOutlined />}>上传文件</Button>
      </Upload>
    }>
      <Table rowKey="id" columns={columns} dataSource={data} loading={loading}
        pagination={{ current: page, total, pageSize: 10, onChange: (p) => setPage(p) }} />
    </Card>
  )
}
