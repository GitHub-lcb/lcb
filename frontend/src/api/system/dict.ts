import request from '../request'
import type { PageResult, SysDictType, SysDictData } from '@/types/api'

export const dictApi = {
  typePage: (params: { page: number; pageSize: number }): Promise<PageResult<SysDictType>> =>
    request.get('/system/dict/type/page', { params }),
  getData: (type: string): Promise<SysDictData[]> =>
    request.get(`/system/dict/data/${type}`),
  addType: (data: Partial<SysDictType>): Promise<void> =>
    request.post('/system/dict/type', data),
}
