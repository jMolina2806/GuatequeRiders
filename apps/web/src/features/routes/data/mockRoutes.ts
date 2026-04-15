import type { Route } from '../types/route.types'

export const mockRoutes: Route[] = [
  {
    id: 1,
    title: 'Ruta Sierra Nevada',
    description: 'Salida de fin de semana por carretera de montaña.',
    date: '2026-04-20',
    time: '09:00',
    origin: 'Granada',
    destination: 'Sierra Nevada',
    distanceKm: 120,
    difficulty: 'Media',
    status: 'Abierta',
    maxParticipants: 20,
    participants: [
      { id: 1, name: 'Marc' },
      { id: 2, name: 'Emilio' },
      { id: 3, name: 'Rocío' },
    ],
  },
  {
    id: 2,
    title: 'Ruta Costa Tropical',
    description: 'Ruta tranquila con parada para almorzar.',
    date: '2026-04-27',
    time: '10:00',
    origin: 'Granada',
    destination: 'Almuñécar',
    distanceKm: 95,
    difficulty: 'Fácil',
    status: 'Abierta',
    maxParticipants: 25,
    participants: [
      { id: 4, name: 'Javi' },
      { id: 5, name: 'Luis' },
    ],
  },
]