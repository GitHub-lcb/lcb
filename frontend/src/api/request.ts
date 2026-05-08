import axios from 'axios'
import { message } from 'antd'

const request = axios.create({ baseURL: '/api', timeout: 30000 })

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  (res) => {
    if (res.data.code !== 200) {
      message.error(res.data.msg)
      return Promise.reject(new Error(res.data.msg))
    }
    return res.data.data
  },
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    message.error(err.response?.data?.msg || '请求失败')
    return Promise.reject(err)
  }
)

export default request
