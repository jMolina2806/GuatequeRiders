const MESSAGES: Record<string, string> = {
  'Access denied': 'Acceso denegado',
  'Unauthorized': 'No autorizado',
  'Invalid request body': 'Solicitud no válida',
  'Internal server error': 'Error interno del servidor',
  'Invalid username or password': 'Usuario o contraseña incorrectos',
  'Invalid credentials': 'Credenciales inválidas',

  'Route is full': 'La ruta está completa',
  'Route is not open for registration': 'La ruta está cerrada para inscripción',
  'Already registered to this route': 'Ya estás apuntado a esta ruta',
  'At least one organizer is required before publishing':
    'Se necesita al menos un organizador para publicar',
  'Cannot publish a route with a past date': 'No se puede publicar una ruta con fecha pasada',
  'Meeting and departure times are required before publishing':
    'Faltan la hora de quedada y de salida para publicar',
  'Meeting and departure times are required': 'La hora de quedada y de salida son obligatorias',
  'Departure time must be after meeting time': 'La hora de salida debe ser posterior a la de quedada',
  'Only ADMIN users can be route organizers': 'Solo los administradores pueden ser organizadores de rutas',
  'Only DRAFT routes can be published': 'Solo se pueden publicar rutas en borrador',

  'Event is full': 'El evento está completo',
  'Event is not open for registration': 'El evento está cerrado para inscripción',
  'Already registered to this event': 'Ya estás apuntado a este evento',

  'Cannot remove the last admin': 'No puedes quitar el último administrador',
  'Cannot change role: user is organizer of one or more routes':
    'No se puede cambiar el rol: el usuario es organizador de una o más rutas',
}

export function translateApiMessage(message: string): string {
  if (MESSAGES[message]) return MESSAGES[message]

  if (message.startsWith('Registration not found for route')) {
    return 'No estás apuntado a esta ruta'
  }
  if (message.startsWith('Registration not found for event')) {
    return 'No estás apuntado a este evento'
  }
  if (message.startsWith('Username already exists')) {
    return 'El nombre de usuario ya está en uso'
  }
  if (message.startsWith('Email already registered')) {
    return 'El email ya está registrado'
  }
  if (message.startsWith('Route not found')) {
    return 'Ruta no encontrada'
  }
  if (message.startsWith('Event not found')) {
    return 'Evento no encontrado'
  }
  if (message.startsWith('User not found')) {
    return 'Usuario no encontrado'
  }

  return message
}
