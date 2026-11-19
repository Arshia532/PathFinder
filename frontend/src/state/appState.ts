import type { AlgorithmId, Cell, GridMap } from '../api/types'

export type Tool = 'start' | 'end' | 'wall' | 'erase'

export type AppState = {
    map: GridMap
    tool: Tool
    algorithm: AlgorithmId
}

export type AppAction =
    | { type: 'setMap'; map: GridMap }
    | { type: 'setTool'; tool: Tool }
    | { type: 'setAlgorithm'; algorithm: AlgorithmId }
    | { type: 'editCell'; cell: Cell }

export const initialState: AppState = {
    map: emptyMap(30, 21),
    tool: 'wall',
    algorithm: 'bfs',
}

export function emptyMap(width: number, height: number): GridMap {
    return {
        width, height,
        start: { row: 1, column: 1 },
        end: { row: height - 2, column: width - 2 },
        walls: [],
    }
}

function sameCell(a: Cell, b: Cell) {
    return a.row === b.row && a.column === b.column
}

export function appReducer(state: AppState, action: AppAction): AppState {
    switch (action.type) {
        case 'setMap':
            return { ...state, map: action.map }
        case 'setTool':
            return { ...state, tool: action.tool }
        case 'setAlgorithm':
            return { ...state, algorithm: action.algorithm }
        case 'editCell': {
            const map = state.map
            const cell = action.cell
            const walls = map.walls.filter(wall => !sameCell(wall, cell))
            if (state.tool === 'wall') {
                if (sameCell(cell, map.start) || sameCell(cell, map.end) || walls.length < map.walls.length) {
                    return state
                }
                return { ...state, map: { ...map, walls: [...walls, cell] } }
            }
            if (state.tool === 'erase') {
                return { ...state, map: { ...map, walls } }
            }
            const other = state.tool === 'start' ? map.end : map.start
            if (sameCell(cell, other)) return state
            return { ...state, map: { ...map, [state.tool]: cell, walls } }
        }
    }
}
