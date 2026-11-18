import type { User } from '@/features/auth/types/auth.types'
import './ProfileHeader.css'

type ProfileHeaderProps = {
  user: User
}

function initialsOf(user: User): string {
  return user.username.slice(0, 2).toUpperCase()
}

function formatDate(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString('es-ES', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
}

export default function ProfileHeader({ user }: ProfileHeaderProps) {
  return (
    <div className="profile-header">
      <div className="profile-header__avatar">{initialsOf(user)}</div>

      <div className="profile-header__info">
        <div className="profile-header__name-row">
          <h1 className="profile-header__username">{user.username}</h1>
          <span className={`profile-header__role profile-header__role--${user.role.toLowerCase()}`}>
            {user.role}
          </span>
        </div>
        <p className="profile-header__email">{user.email}</p>
        <p className="profile-header__since">
          Miembro desde {formatDate(user.createdAt)}
        </p>
      </div>
    </div>
  )
}
