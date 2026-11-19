import { useEffect, useMemo, useState } from 'react'
import { mockSearch } from '../api/mockApi'
import type { AlgorithmId, GridMap, SearchRequest, SearchResponse } from '../api/types'

export type SearchState = {
    result: SearchResponse | null
    error: string | null
    pending: boolean 
    resultRequest: SearchRequest | null 
}

type Settled = {
    request: SearchRequest 
    result: SearchResponse | null
    error: string | null
}

export function useSearch(
    map: GridMap,
    algorithm: AlgorithmId,
    bidirectional: boolean,
    paused: boolean,
    
): SearchState {
    const request = useMemo<SearchRequest>(
        () => ({ ...map, algorithm, bidirectional }),
        [map, algorithm, bidirectional],
    )
    const [settled, setSettled] = useState<Settled | null>(null)
    const upToDate = settled?.request === request

    useEffect(() => {
        if (paused || upToDate) return

        const controller = new AbortController()
        mockSearch(request, controller.signal).then(
            (result) => {
                if (!controller.signal.aborted) setSettled({ request, result, error: null })
            },
            (error: unknown) => {
                if (controller.signal.aborted) return 
                const message = error instanceof Error ? error.message : 'Search failed'
                setSettled({ request, result: null, error: message })
            },
        )
        return () => controller.abort()
    }, [request, paused, upToDate])

    return {
        result: settled?.result ?? null,
        error: settled?.error ?? null,
        pending: !upToDate,
        resultRequest: settled?.request ?? null,
    }
}