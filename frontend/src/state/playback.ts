import type { Cell, GridMap, SearchResponse } from '../api/types'

export function cellIndex(cell: Cell, width: number) {
    return cell.row * width + cell.column
}

export function playbackFrame(map: GridMap, result: SearchResponse | null, cursor: number) {
    const visited = new Set<number>()
    const frontier = new Set<number>()
    let current: number | undefined
    for (const step of result?.steps.slice(0, cursor) ?? []) {
        current = cellIndex(step.expanded, map.width)
        visited.add(current)
        frontier.delete(current)
        for (const cell of step.discovered) frontier.add(cellIndex(cell, map.width))
    }

    const finished = result !== null && cursor === result.steps.length
    const path = new Set(finished ? result.path.map(cell => cellIndex(cell, map.width)) : [])
    return { visited, frontier, current, path, finished }
}
