import request from './request'
import type { LoginParams, LoginResult, UserInfoResult } from '../types/api'

export const authApi = {
  login: (data: LoginParams): Promise<LoginResult> =>
    request.post('/auth/login', data),
  logout: () => request.post('/auth/logout'),
  getInfo: (): Promise<UserInfoResult> =>
    request.get('/auth/info'),
}
