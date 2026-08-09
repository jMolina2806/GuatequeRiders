import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
} from '../types/auth.types'

const API_BASE = '/api/auth'

type ApiErrorBody = {
  message?: string
  errors?: string[]
}

function getErrorMessage(body: ApiErrorBody | null, status: number): string {
  const message = body?.message
  if (message) {
    if (message === 'Invalid username or password') {
      return 'Usuario o contraseña incorrectos'
    }
    if (message.startsWith('Username already exists')) {
      return 'El nombre de usuario ya está en uso'
    }
    if (message.startsWith('Email already registered')) {
      return 'El email ya está registrado'
    }
    if (message === 'Validation failed' && body.errors && body.errors.length > 0) {
      return body.errors[0]
    }
    return message
  }
  if (status === 401) {
    return 'Credenciales inválidas'
  }
  return `Error ${status}`
}

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const body = await response.json().catch(() => null) as ApiErrorBody | null
    throw new ApiError(getErrorMessage(body, response.status), response.status)
  }
  return response.json() as Promise<T>
}

export async function login(data: LoginRequest): Promise<AuthResponse> {
  try {
    const response = await fetch(`${API_BASE}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
    return handleResponse<AuthResponse>(response)
  } catch (err) {
    if (err instanceof ApiError) throw err
    throw new ApiError('Error de conexión, intenta de nuevo', 0)
  }
}

export async function register(data: RegisterRequest): Promise<AuthResponse> {
  try {
    const response = await fetch(`${API_BASE}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
    return handleResponse<AuthResponse>(response)
  } catch (err) {
    if (err instanceof ApiError) throw err
    throw new ApiError('Error de conexión, intenta de nuevo', 0)
  }
}

export async function getCurrentUser(token: string): Promise<User> {
  try {
    const response = await fetch(`${API_BASE}/me`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    return handleResponse<User>(response)
  } catch (err) {
    if (err instanceof ApiError) throw err
    throw new ApiError('Error de conexión, intenta de nuevo', 0)
  }
}
