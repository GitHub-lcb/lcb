import request from './request'
import type { PageResult } from '@/types/api'

interface SysFile {
  id: number
  originalName: string
  fileSize: number
  fileType: string
  url: string
  createTime: string
}

export const fileApi = {
  page: (params: { page: number; pageSize: number }): Promise<PageResult<SysFile>> =>
    request.get('/file/page', { params }),
  upload: (formData: FormData): Promise<SysFile> =>
    request.post('/file/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  remove: (id: number): Promise<void> => request.delete(`/file/${id}`),
}
