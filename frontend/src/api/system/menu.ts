import request from '../request'
import type { SysMenu } from '@/types/api'

export const menuApi = {
  tree: (): Promise<SysMenu[]> => request.get('/system/menu/tree'),
  add: (data: Partial<SysMenu>): Promise<void> => request.post('/system/menu', data),
  edit: (data: Partial<SysMenu>): Promise<void> => request.put('/system/menu', data),
  remove: (id: number): Promise<void> => request.delete(`/system/menu/${id}`),
}
