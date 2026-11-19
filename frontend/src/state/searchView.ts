import type { Cell, SearchRequest, SearchResponse } from '../api/types'
import type { SearchState } from '../hooks/useSearch'

export type CellMark = 'visited' | 'frontier' | 'current' | 'path'

export type StepInfo = {
    verb: string
    cell: Cell
    index: number // steps shown so far
    total: number
}

export type Frame = {
    marks: Map<string, CellMark> // key: cellKey(cell)
    visited: number
    frontier: number
    pathLength: number | null // only on the finished frame, and only if found
    step: StepInfo
}

export type ResultMessage = {
    tone: 'info' | 'success' | 'error'
    text: string
}

export const cellKey = (cell: Cell) => `${cell.row},${cell.column}`

/** Replays the first `stepCount` steps. When every step is shown, the path is drawn too. */
export function buildFrame(request: SearchRequest, response: SearchResponse, stepCount: number): Frame {
    const shown = response.steps.slice(0, stepCount)
    const finished = shown.length === response.steps.length
    const marks = new Map<string, CellMark>()

    // Before step 0 the frontier holds the start (and the end, for bidirectional)
    marks.set(cellKey(request.start), 'frontier')
    if (request.bidirectional) marks.set(cellKey(request.end), 'frontier')

    for (const step of shown) {
        marks.set(cellKey(step.expanded), 'visited')
        for (const cell of step.discovered) marks.set(cellKey(cell), 'frontier')
    }

    let frontier = 0
    for (const mark of marks.values()) if (mark === 'frontier') frontier++

    const last = shown.at(-1)
    if (finished) {
        for (const cell of response.path) marks.set(cellKey(cell), 'path')
    } else if (last) {
        marks.set(cellKey(last.expanded), 'current')
    }

    return {
        marks,
        visited: shown.length,
        frontier,
        pathLength: finished && response.found ? response.stats.pathLength : null,
        step: last
            ? { verb: 'Expanded', cell: last.expanded, index: shown.length, total: response.steps.length }
            : { verb: 'Start at', cell: request.start, index: 0, total: response.steps.length },
    }
}

export function resultMessage(search: SearchState): ResultMessage {
    if (search.pending) return { tone: 'info', text: 'Searching…' }
    if (search.error) return { tone: 'error', text: search.error }
    if (!search.result) return { tone: 'info', text: 'Searching…' }
    if (!search.result.found) return { tone: 'error', text: 'No path: the end cannot be reached' }
    const moves = search.result.stats.pathLength
    return { tone: 'success', text: `Path found: ${moves} ${moves === 1 ? 'move' : 'moves'}` }
}