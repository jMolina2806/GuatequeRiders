export type UserRole = 'admin' | 'user'

export type CurrentUser = {
  id: string
  name: string
  role: UserRole
}
