import type { ApiError, GridMap, SearchRequest, SearchResponse } from './types'

export type GenerateMode = 'maze' | 'random'

export function generateMap(width: number, height: number, seed: string, mode: GenerateMode) {
    return post<GridMap>('/api/maps/generate', { width, height, seed, mode })
}

export function searchMap(request: SearchRequest) {
    return post<SearchResponse>('/api/search', request)
}

async function post<T>(path: string, body: object): Promise<T> {
    const response = await fetch(path, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    })
    if (!response.ok) {
        const error = await response.json().catch(() => null) as ApiError | null
        throw new Error(error?.message ?? 'Could not reach the PathFinder API.')
    }
    return response.json() as Promise<T>
}
