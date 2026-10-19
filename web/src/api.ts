import axios from 'axios'

export interface PokemonStat {
  name: string
  baseStat: number
}

export interface EvolutionStage {
  name: string
  evolvesTo: EvolutionStage[]
}

export interface PokemonSummary {
  id: number
  name: string
  spriteUrl: string
  category: string
  weightKg: number
  abilities: string[]
}

export interface PokemonDetail extends Omit<PokemonSummary, 'spriteUrl'> {
  imageUrl: string
  description: string
  stats: PokemonStat[]
  evolutionChain: EvolutionStage
}

export interface LocalPokemon extends Omit<PokemonDetail, 'evolutionChain'> {
  evolutionChain?: EvolutionStage
  localName: string | null
  region: string | null
  tags: string[]
  syncedAt: string
}

export interface PageResult<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface AuthResponse {
  id: string
  username: string
  displayName: string
  accessToken: string
  tokenType: string
  expiresAt: string
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15_000,
})

function authorization(token?: string) {
  return token ? { Authorization: `Bearer ${token}` } : undefined
}

export function getErrorMessage(error: unknown) {
  if (axios.isAxiosError<{ message?: string }>(error)) {
    if (error.response?.data?.message) return error.response.data.message
    if (!error.response) return 'Could not reach the API. Check that the backend is running.'
    return `The request failed (${error.response.status}). Please try again.`
  }
  return error instanceof Error ? error.message : 'Something went wrong. Please try again.'
}

export async function getPokemonPage(page: number, signal?: AbortSignal) {
  const { data } = await api.get<PageResult<PokemonSummary>>('/pokemon', {
    params: { page, size: 20 },
    signal,
  })
  return data
}

export async function getLocalPokemonPage(page: number, signal?: AbortSignal) {
  const { data } = await api.get<PageResult<LocalPokemon>>('/local/pokemon', {
    params: { page, size: 20 },
    signal,
  })
  return data
}

export async function getPokemon(idOrName: string, local = false) {
  const key = encodeURIComponent(idOrName)
  if (local) {
    const [localResponse, catalogResponse] = await Promise.all([
      api.get<LocalPokemon>(`/local/pokemon/${key}`),
      api.get<PokemonDetail>(`/pokemon/${key}`),
    ])
    return { ...localResponse.data, evolutionChain: catalogResponse.data.evolutionChain }
  }
  const { data } = await api.get<PokemonDetail>(`/pokemon/${key}`)
  return data
}

export async function updateLocalPokemon(
  idOrName: string,
  token: string,
  enrichment: { localName: string; region: string; tags: string[] },
) {
  const { data } = await api.put<LocalPokemon>(
    `/local/pokemon/${encodeURIComponent(idOrName)}`,
    enrichment,
    { headers: authorization(token) },
  )
  return data
}

export async function syncPokemon(idOrName: string, token: string) {
  const { data } = await api.post<LocalPokemon>(
    `/local/pokemon/${encodeURIComponent(idOrName)}/sync`,
    undefined,
    { headers: authorization(token) },
  )
  return data
}

export async function login(username: string, password: string) {
  const { data } = await api.post<AuthResponse>('/auth/login', { username, password })
  return data
}

export async function register(username: string, password: string, displayName: string) {
  const { data } = await api.post<AuthResponse>('/auth/register', {
    username,
    password,
    displayName,
  })
  return data
}
