import { create } from 'zustand'

interface AppState {
  permissions: string[]
  setPermissions: (perms: string[]) => void
}

export const useStore = create<AppState>((set) => ({
  permissions: [],
  setPermissions: (permissions) => set({ permissions }),
}))
