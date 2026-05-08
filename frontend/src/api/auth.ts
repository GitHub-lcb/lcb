import request from './request'

export const authApi = {
  login: (data: { username: string; password: string }) =>
    request.post('/auth/login', data),
  logout: () => request.post('/auth/logout'),
  getInfo: (): Promise<{ user: any; permissions: string[] }> =>
    request.get('/auth/info'),
}
