import request from '../request'

export const userApi = {
  page: (params: any) => request.get('/system/user/page', { params }),
  get: (id: number) => request.get(`/system/user/${id}`),
  add: (data: any) => request.post('/system/user', data),
  edit: (data: any) => request.put('/system/user', data),
  remove: (id: number) => request.delete(`/system/user/${id}`),
  resetPassword: (id: number, password: string) => request.put(`/system/user/${id}/reset-password`, { password }),
}
