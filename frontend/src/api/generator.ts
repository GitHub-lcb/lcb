import request from './request'
import type { PageResult } from '@/types/api'

interface GenTable {
  id: number
  tableName: string
  tableComment: string
  className: string
  createTime: string
}

export const generatorApi = {
  tablePage: (params: { page: number; pageSize: number }): Promise<PageResult<GenTable>> =>
    request.get('/generator/table/page', { params }),
  generateCode: (tableId: number): Promise<void> =>
    request.post(`/generator/code/${tableId}`),
}
