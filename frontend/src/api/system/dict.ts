import request from '../request'

export const dictApi = {
  typePage: (params: any) => request.get('/system/dict/type/page', { params }),
  getData: (type: string) => request.get(`/system/dict/data/${type}`),
  addType: (data: any) => request.post('/system/dict/type', data),
}
