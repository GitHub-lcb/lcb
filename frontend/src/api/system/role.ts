import request from '../request'
import type { PageResult, SysRole, RoleMenuParams } from '../../types/api'

export const roleApi = {
  page: (params: { page: number; pageSize: number }): Promise<PageResult<SysRole>> =>
    request.get('/system/role/page', { params }),
  add: (data: Partial<SysRole>): Promise<void> => request.post('/system/role', data),
  edit: (data: Partial<SysRole>): Promise<void> => request.put('/system/role', data),
  remove: (id: number): Promise<void> => request.delete(`/system/role/${id}`),
  assignMenu: (data: RoleMenuParams): Promise<void> => request.put('/system/role/menu', data),
}
