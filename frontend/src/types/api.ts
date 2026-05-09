export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface SysUser {
  id: number
  username: string
  password?: string
  nickname: string
  email: string
  phone: string
  avatar: string
  status: number
  createTime: string
}

export interface SysRole {
  id: number
  roleName: string
  roleKey: string
  dataScope: number
  status: number
  createTime: string
}

export interface SysMenu {
  id: number
  menuName: string
  permission: string
  path: string
  component: string
  icon: string
  parentId: number
  sort: number
  menuType: string
  status: number
  children: SysMenu[]
}

export interface SysDictType {
  id: number
  dictName: string
  dictType: string
  status: number
  createTime: string
}

export interface SysDictData {
  id: number
  dictType: string
  dictLabel: string
  dictValue: string
  dictSort: number
  cssClass: string
  status: number
}

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  user: SysUser
}

export interface UserInfoResult {
  user: SysUser
  permissions: string[]
}

export interface RoleMenuParams {
  roleId: number
  menuIds: number[]
}
