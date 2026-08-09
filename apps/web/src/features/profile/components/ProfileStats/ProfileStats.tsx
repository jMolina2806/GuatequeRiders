import type { ProfileStats } from '@/features/profile/types/profile.types'
import './ProfileStats.css'

type ProfileStatsProps = {
  stats: ProfileStats
}

export default function ProfileStats({ stats }: ProfileStatsProps) {
  return (
    <div className="profile-stats">
      <div className="profile-stats__item">
        <span className="profile-stats__value">{stats.routesCount}</span>
        <span className="profile-stats__label">Rutas apuntadas</span>
      </div>
      <div className="profile-stats__item">
        <span className="profile-stats__value">{stats.eventsCount}</span>
        <span className="profile-stats__label">Eventos apuntados</span>
      </div>
      <div className="profile-stats__item">
        <span className="profile-stats__value">{stats.totalKm} km</span>
        <span className="profile-stats__label">Kilómetros aprox.</span>
      </div>
    </div>
  )
}
