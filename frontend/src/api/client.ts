import type { ApiError, GridMap, SearchRequest, SearchResponse } from './types'

async function send(path: string, body: string, signal?: AbortSignal): Promise<Response> {
    let response: Response
    try {
        response = await fetch(path, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body,
            signal,
        })
    } catch (error) {
        if (signal?.aborted) throw error
        throw new Error("Can't reach the server. Is the backend running?", { cause: error })
    }
    if (!response.ok) throw new Error(await readErrorMessage(response))
    return response
}

async function readErrorMessage(response: Response): Promise<string> {
    try {
        const data = (await response.json()) as Partial<ApiError>
        if (typeof data.message === 'string' && data.message) return data.message
    } catch {
        // Body wasn't JSON; use a generic message below
    }
    return response.status >= 500
        ? "The server isn't responding or ran into a problem. Is the backend running?"
        : `Request failed (${response.status}).`
}

export async function searchRequest(request: SearchRequest, signal?: AbortSignal): Promise<SearchResponse> {
    const response = await send('/api/search', JSON.stringify(request), signal)
    return (await response.json()) as SearchResponse
}

export async function exportMapRequest(map: GridMap): Promise<string> {
    const response = await send('/api/maps/export', JSON.stringify(map))
    return response.text()
}

export async function importMapRequest(text: string): Promise<GridMap> {
    const response = await send('/api/maps/import', text)
    return (await response.json()) as GridMap
}