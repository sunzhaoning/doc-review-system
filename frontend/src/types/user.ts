export interface UserInfo {
  id: number
  username: string
  email: string
  phone: string
  realName: string
  avatar: string
  deptId: number
  status: number
  roles: string[]
  permissions: string[]
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
}
