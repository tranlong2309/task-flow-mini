import type { User } from '../../types/domain'

export function Avatar({ user, small = false }: { user?: User; small?: boolean }) {
  if (!user) {
    return <span className={`avatar avatar-muted ${small ? 'small' : ''}`}>?</span>
  }
  return (
    <span className={`avatar ${small ? 'small' : ''}`} style={{ background: user.color }}>
      {user.initials}
    </span>
  )
}
