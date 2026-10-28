const ALLOWED_CUSTOM_NAME = /^[\p{L}\p{N}](?:[\p{L}\p{N} '-]*[\p{L}\p{N}])?$/u
const CONTROL_CHARACTER = /\p{Cc}/u

export function normalizeCustomName(value: string | null | undefined): string | null {
  if (value === null || value === undefined) return null
  const normalized = value.trim()
  return normalized || null
}

export function validateCustomName(value: string | null | undefined): string | null {
  if (value === null || value === undefined || value.trim() === '') return null
  if (CONTROL_CHARACTER.test(value)) return 'El nombre personalizado no puede contener caracteres de control.'

  const normalized = normalizeCustomName(value)
  if (!normalized) return null
  if ([...normalized].length > 20) return 'El nombre personalizado no puede superar 20 caracteres.'
  if (!ALLOWED_CUSTOM_NAME.test(normalized)) {
    return 'Usa solo letras, números, espacios, guiones y apóstrofes.'
  }
  return null
}
