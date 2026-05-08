import request from '../request'

export const roleApi = {
  page: (params: any) => request.get('/system/role/page', { params }),
  add: (data: any) => request.post('/system/role', data),
  edit: (data: any) => request.put('/system/role', data),
  remove: (id: number) => request.delete(`/system/role/${id}`),
  assignMenu: (data: { roleId: number; menuIds: number[] }) => request.put('/system/role/menu', data),
}
