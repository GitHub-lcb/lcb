import { useStore } from '../store'

interface AuthProps {
  permission: string
  children: React.ReactNode
}

export default function Auth({ permission, children }: AuthProps) {
  const permissions = useStore((s) => s.permissions)
  if (!permissions.includes(permission)) return null
  return <>{children}</>
}
