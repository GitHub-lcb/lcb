import { create } from 'zustand'
import { authApi } from '@/api/auth'

interface AppState {
  permissions: string[]
  setPermissions: (perms: string[]) => void
  fetchUserInfo: () => Promise<void>
  logout: () => void
}

export const useStore = create<AppState>((set) => ({
  permissions: [],
  setPermissions: (permissions) => set({ permissions }),
  fetchUserInfo: async () => {
    try {
      const res = await authApi.getInfo()
      set({ permissions: res.permissions || [] })
    } catch {
      set({ permissions: [] })
    }
  },
  logout: () => {
    localStorage.removeItem('token')
    set({ permissions: [] })
  },
}))
