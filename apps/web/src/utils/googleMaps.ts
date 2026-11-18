const MAPS_SEARCH_BASE = 'https://www.google.com/maps/search/'
const MAPS_DIRECTIONS_BASE = 'https://www.google.com/maps/dir/'

export function getGoogleMapsSearchUrl(location: string): string {
  const params = new URLSearchParams({ api: '1', query: location })
  return `${MAPS_SEARCH_BASE}?${params.toString()}`
}

export function getGoogleMapsDirectionsUrl(origin: string, destination: string): string {
  const params = new URLSearchParams({ api: '1', origin, destination })
  return `${MAPS_DIRECTIONS_BASE}?${params.toString()}`
}
