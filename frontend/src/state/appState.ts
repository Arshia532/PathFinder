import type { AlgorithmId, Cell, GridMap } from "../api/types";

export type Tool = 'start' | 'end' | 'wall' | 'erase'

export type AppState = {
    map: GridMap
    tool: Tool
    algorithm: AlgorithmId
    bidirectional: boolean
    painting: boolean
}

export type AppAction = 
| {type: 'setTool'; tool: Tool}
| {type: 'setAlgorithm'; algorithm: AlgorithmId}
| {type: 'setBidirectional'; bidirectional: boolean}
| {type: 'paintStart'; cell: Cell}
| {type: 'paintMove'; cell: Cell}
| {type: 'paintEnd'}
| {type: 'resetMap'}

export function createMap(width: number, height: number): GridMap {
    return {
        width,
        height,
        start: {row: height - 6, column: 3},
        end: {row: 5, column: width - 4},
        walls: [],
    }
}


export const initialState: AppState = {
    map: createMap(30, 21),
    tool: 'wall',
    algorithm: 'astar',
    bidirectional: false,
    painting: false,
}

export function appReducer(state: AppState, action: AppAction): AppState {
    switch (action.type) {
        case 'setTool': 
            return {...state, tool: action.tool}
        case 'setAlgorithm': 
            return {...state, algorithm: action.algorithm}
        case 'setBidirectional': 
            return {...state, bidirectional: action.bidirectional}
        case 'paintStart':
            return {...state, painting: true, map: paintCell(state.map, state.tool, action.cell)}
        case 'paintMove': {
            if (!state.painting) return state
            const movingEndpoint = state.tool === 'start' || state.tool === 'end'
            if (movingEndpoint && isWall(state.map, action.cell)) return state
            const map = paintCell(state.map, state.tool, action.cell)
            return map === state.map ? state : {...state, map}
        }
        case 'paintEnd':
            return state.painting ? {...state, painting: false} : state
        case 'resetMap':
            return {...state, map: createMap(state.map.width, state.map.height)}
    } 
}

function isWall(map: GridMap, cell: Cell) {
    return map.walls.some((wall) => sameCell(wall, cell))
}

function sameCell(a: Cell, b: Cell) {
    return a.row === b.row && a.column === b.column
}

function paintCell(map: GridMap, tool: Tool, cell: Cell): GridMap {
    const isStart = sameCell(cell, map.start)
    const isEnd = sameCell(cell, map.end)
    const cellIsWall = isWall(map, cell)
    const wallsWithoutCell = map.walls.filter((wall) => !sameCell(wall, cell))

    switch (tool) {
        case 'start':
            if (isStart || isEnd) return map
            return {...map, start: cell, walls: wallsWithoutCell}
        case 'end':
            if (isEnd || isStart) return map
            return {...map, end: cell, walls: wallsWithoutCell}
        case 'wall':
            if (isStart || isEnd || cellIsWall) return map
            return {...map, walls: [...map.walls, cell]}
        case 'erase':
            if (!cellIsWall) return map
            return {...map, walls: wallsWithoutCell}
    }
}

