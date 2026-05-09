import request from '../request'
import type { PageResult } from '@/types/api'

interface AuditLogItem {
  id: number
  username: string
  operation: string
  method: string
  duration: number
  status: number
  createTime: string
}

export const auditLogApi = {
  page: (params: { page: number; pageSize: number }): Promise<PageResult<AuditLogItem>> =>
    request.get('/monitor/audit-log/page', { params }),
}
