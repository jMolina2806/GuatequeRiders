export type ProfileStats = {
  routesCount: number
  eventsCount: number
  totalKm: number
}

export type NotificationPreferences = {
  merchNotifications: boolean
  routeNotifications: boolean
  eventNotifications: boolean
  circuitNotifications: boolean
}

export type NotificationCategory = 'MERCH' | 'ROUTES' | 'EVENTS' | 'CIRCUITS'
