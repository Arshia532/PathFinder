/** Zero-based cell. Rows grow downward, columns grow right (same as Java Position). */
export type Cell = {
    row: number
    column: number
}

export type AlgorithmId = 'bfs' | 'dfs' | 'astar' | 'dijkstra'

/** Which search produced a step. Always 'forward' unless bidirectional is on. */
export type SearchSide = 'forward' | 'backward'

/** The editable map. Also the shape of a saved map file. */
export type GridMap = {
    width: number
    height: number
    start: Cell
    end: Cell
    walls: Cell[] 
}

/** POST /api/search body: the whole map plus the search settings. */
export type SearchRequest = GridMap & {
    algorithm: AlgorithmId
    bidirectional: boolean
}

/** One expansion: the cell taken off the frontier, and cells that entered it for the first time. */
export type SearchStep = {
    expanded: Cell
    discovered: Cell[]
    side: SearchSide
}

export type SearchStats = {
    visited: number //steps.length
    pathLength: number | null //path.length - 1
    pathCost: number | null //sum of entry costs, its equal to pathLength until we do terrain costs
}

export type SearchResponse = {
    found: boolean
    steps: SearchStep []
    path: Cell[]
    stats: SearchStats
}

export type ApiError = {
    message: string
}

export type RandomMapRequest = {
    width: number
    height: number
    seed: number
}