import request from '../request'

export const menuApi = {
  tree: () => request.get('/system/menu/tree'),
  add: (data: any) => request.post('/system/menu', data),
  edit: (data: any) => request.put('/system/menu', data),
  remove: (id: number) => request.delete(`/system/menu/${id}`),
}
