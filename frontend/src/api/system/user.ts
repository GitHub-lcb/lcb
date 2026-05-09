import request from '../request'
import type { PageResult, SysUser } from '../../types/api'

export const userApi = {
  page: (params: { page: number; pageSize: number }): Promise<PageResult<SysUser>> =>
    request.get('/system/user/page', { params }),
  get: (id: number): Promise<SysUser> => request.get(`/system/user/${id}`),
  add: (data: Partial<SysUser>): Promise<void> => request.post('/system/user', data),
  edit: (data: Partial<SysUser>): Promise<void> => request.put('/system/user', data),
  remove: (id: number): Promise<void> => request.delete(`/system/user/${id}`),
  resetPassword: (id: number, password: string): Promise<void> =>
    request.put(`/system/user/${id}/reset-password`, { password }),
}
